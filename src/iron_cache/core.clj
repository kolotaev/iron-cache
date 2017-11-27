(ns iron-cache.core
  (:refer-clojure :exclude [get list])
  (:use iron-cache.util)
  (:require [clj-http.client :as http-client]))


;;; Configuration defaults ;;;

(def ^:const ^:private ROOT_URL "https://cache-aws-us-east-1.iron.io")

(def ^:const ^:private DEFAULTS
  {:host ROOT_URL
   :port 443
   :api_version 1
   :http-options {:client-params {"http.useragent" "iron_cache_clj_client"}
                  :content-type :json
                  :accept :json
                  :as :json
                  :throw-exceptions false
                  :coerce :always}})


;;; Protocols ;;;

(defprotocol Cache
  "Iron cache instance manipulation"
  (list [this] [this cbs] "Get a list off all caches")
  (info [this cache] [this cache cbs] "Get information about a cache")
  (delete! [this cache] [this cache cbs] "Delete a cache")
  (clear! [this cache] [this cache cbs] "Clear a cache"))


(defprotocol Key
  "Iron cache instance keys manipulation"
  (get [this cache key] [this cache key cbs] "Get a value stored in a key from a cache")
  (put [this cache key val] [this cache key val cbs] "Add key/value pair to a cache")
  (incr [this cache key val] [this cache key val cbs] "Increment value in a cache stored at key by a specified amount")
  (del [this cache key] [this cache key cbs] "Delete a value from a cache stored at key"))


;;; Clent record ;;;

(defrecord Client [http config]

  Cache

  (list [this]
    (list this nil))
  (list [this cbs]
    (http :get "caches" cbs))

  (info [this cache]
    (info this cache nil))
  (info [this cache cbs]
    (http :get (format-str "caches/%s" cache) cbs))

  (delete! [this cache]
    (delete! this cache nil))
  (delete! [this cache cbs]
    (http :delete (format-str "caches/%s" cache) cbs))

  (clear! [this cache]
    (clear! this cache nil))
  (clear! [this cache cbs]
    (http :post (format-str "caches/%s/clear" cache) cbs))

  Key

  (get [this cache key]
    (get this cache key nil))
  (get [this cache key cbs]
    (http :get (format-str "caches/%s/items/%s" cache key) cbs))

  (put [this cache key val]
    (put this cache key val nil))
  (put [this cache key val cbs]
    (http :put (format-str "caches/%s/items/%s" cache key) val cbs))

  (incr [this cache key val]
    (incr this cache key val nil))
  (incr [this cache key val cbs]
    (http :post (format-str "caches/%s/items/%s" cache key) {:amount val} cbs))

  (del [this cache key]
    (del this cache key nil))
  (del [this cache key cbs]
    (http :delete (format-str "caches/%s/items/%s" cache key cbs))))


;;; Main functionality ;;;

(defn- options-from-env
  "Get token and project name out of environment variables."
  []
  {:project (env "IRON_CACHE_PROJECT")
   :token (env "IRON_CACHE_TOKEN")})


(defn- validate-options
  "Validates input options. Throws ex if options are inappropriate for client to work."
  [{:keys [project token] :as config}]
  (when-not project
    (throw (ex-info "A project must be specified." {:given config})))
  (when-not token
    (throw (ex-info "An OAuth2 token must be provided." {:given config})))
  config)


(defn- process-response
  "Processes the response from the server"
  [resp]
  (let [status (:status resp)
        body (:body resp)]
    (if (= (/ status 100) 2)
      {:status status, :msg body}
      {:status status, :msg (:msg body)})))


(defn- make-requester
  "Get a prepared clj http-client to make requests to a server."
  [opts]
  (let [all-options (merge (:http-options opts)
                           {:server-port (:port opts)
                            :headers {"OAuth" (:token opts)
                                      :content-type :json
                                      :accept :json}})
        make-url #(format "%s/%s/%s/%s" (:host opts) (:api_version opts) (:project opts) %)]

    (fn [method uri & [payload {:keys [on-success on-fail]}]]
      (let [async? (or (some? on-success) (some? on-fail))
            http-call (if async?
                        #(http-client/request % on-success on-fail)
                        #(http-client/request %))]
        (-> all-options
          (into {:request-method method
                 :url (make-url uri)
                 :async? async?
                 :body payload})
          http-call
          process-response)))))


(defn new-client
  "Creates a new client with a given options.
  Throws exception if can't create one based on given options."
  [config]
  (let [opts (validate-options (deep-merge DEFAULTS (options-from-env) config))
        http (make-requester opts)]
    (->Client http opts)))
