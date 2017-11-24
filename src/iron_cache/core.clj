(ns iron-cache.core
  (:refer-clojure :exclude [get list])
  (:require [clj-http.client :as http-client]))

(declare deep-merge env)


;;; Configuration defaults ;;;

(def ^:const ^:private ROOT_URL "cache-aws-us-east-1.iron.io")

(def ^:const ^:private DEFAULTS
  {:scheme "https"
   :host ROOT_URL
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
  (list [this] "Get a list off all caches")
  (info [this cache & cbs] "Get information about a cache")
  (delete! [this cache & cbs] "Delete a cache")
  (clear! [this cache & cbs] "Clear a cache"))


(defprotocol Key
  "Iron cache instance keys manipulation"
  (get [this cache key & cbs] "Get a value stored in a key from a cache")
  (put [this cache key val & cbs] "Add key/value pair to a cache")
  (incr [this cache key val & cbs] "Increment value in a cache stored at key by a specified amount")
  (del [this cache key & cbs] "Delete a value from a cache stored at key"))


;;; Clent record ;;;

(defrecord Client [http config]

  Cache

  (list [this]
    (http :get "caches"))

  (info [this cache & cbs]
    (http :get (format "caches/%s" cache)))

  (delete! [this cache & cbs]
    (http :delete (format "caches/%s" cache)))

  (clear! [this cache & cbs]
    (http :post (format "caches/%s/clear" cache)))

  Key

  (get [this cache key & cbs]
    (http :get (format "caches/%s/items/%s" cache key)))

  (put [this cache key val & cbs]
    (http :put (format "caches/%s/items/%s" cache key) val))

  (incr [this cache key val & cbs]
    (http :post (format "caches/%s/items/%s" cache key) {:amount val}))

  (del [this cache key & cbs]
    (http :delete (format "caches/%s/items/%s" cache key))))


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
                            :headers {:oauth-token (:token opts)
                                      :content-type :json
                                      :accept :json}})
        make-url #(format "%s://%s/%s/%s/%s" (:scheme opts) (:host opts) (:api_version opts) (:project opts) %)]

    (fn [method uri & [payload cbs]]
      (-> all-options
        (into {:request-method method
               :url (make-url uri)
               :async? (map? (or cbs nil))
               :body payload})
        http-client/request
        process-response))))


(defn new-client
  "Creates a new client with a given options.
  Throws exception if can't create one based on given options."
  [config]
  (let [opts (validate-options (deep-merge DEFAULTS (options-from-env) config))
        http (make-requester opts)]
    (->Client http opts)))


;;; Utility functions ;;;

(defn- env
  "System/getenv wrapper"
  [key]
  (System/getenv key))


(defn- deep-merge
  "Deeply merges maps so that nested maps are combined rather than replaced."
  [& vs]
  (if (every? map? vs)
    (apply merge-with deep-merge vs)
    (last vs)))