(ns iron-cache.core
  (:refer-clojure :exclude [get list])
  (:use iron-cache.util)
  (:require [clj-http.client :as http-client]
            [cheshire.core :as json]))


;;; Configuration defaults ;;;

(def ^:const ^:private ROOT_URL "https://cache-aws-us-east-1.iron.io")

(def ^:const ^:private DEFAULTS
  {:host ROOT_URL
   :port 443
   :api_version 1
   :parse-callbacks true ;; If you want to use not modified callbacks and manually parse response
   :http-options {:client-params {"http.useragent" "iron_cache_client_clojure"}
                  :content-type :json
                  :accept :json
                  :as :json
                  :throw-exceptions false
                  :coerce :always}})


;;; Protocols ;;;

(defprotocol Cache
  "Iron cache instance manipulation"
  (list [this] [this cbs] "Get a list of all caches in a project")
  (info [this cache] [this cache cbs] "Get general information about a cache")
  (delete! [this cache] [this cache cbs] "Delete a cache and all items in it")
  (clear! [this cache] [this cache cbs] "Delete all items in a cache. This cannot be undone"))


(defprotocol Key
  "Iron cache instance keys manipulation"
  (get [this cache key] [this cache key cbs] "Get a value stored in a key from a cache")
  (put [this cache key data] [this cache key data cbs] "Put an item with specific data into a cache")
  (incr [this cache key val] [this cache key val cbs] "Increments the numeric value of an item in a cache")
  (del [this cache key] [this cache key cbs] "Delete a value from a cache stored at key"))


;;; Client record ;;;

(defrecord Client [http config]

  Cache

  (list [this]
    (list this nil))
  (list [this cbs]
    (http :get "caches" :callbacks cbs))

  (info [this cache]
    (info this cache nil))
  (info [this cache cbs]
    (http :get (format-str "caches/%s" cache) :callbacks cbs))

  (delete! [this cache]
    (delete! this cache nil))
  (delete! [this cache cbs]
    (http :delete (format-str "caches/%s" cache) :callbacks cbs))

  (clear! [this cache]
    (clear! this cache nil))
  (clear! [this cache cbs]
    (http :post (format-str "caches/%s/clear" cache) :callbacks cbs))

  Key

  (get [this cache key]
    (get this cache key nil))
  (get [this cache key cbs]
    (http :get (format-str "caches/%s/items/%s" cache key) :callbacks cbs))

  (put [this cache key data]
    (put this cache key data nil))
  (put [this cache key data cbs]
    (http :put (format-str "caches/%s/items/%s" cache key) :payload data :callbacks cbs))

  (incr [this cache key val]
    (incr this cache key val nil))
  (incr [this cache key val cbs]
    (http :post (format-str "caches/%s/items/%s/increment" cache key) :payload {:amount val} :callbacks cbs))

  (del [this cache key]
    (del this cache key nil))
  (del [this cache key cbs]
    (http :delete (format-str "caches/%s/items/%s" cache key) :callbacks cbs)))


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
        success-status? (= (quot status 100) 2)
        body (:body resp)]
    (cond
      (empty? resp)        {:status 0, :msg "Response is empty. Something went wrong."}
      success-status?      body
      :else                (assoc body :status status))))


(defn- wrap-in-process-response
  "Conditionally wrap given function in response-process"
  [process? f]
  (if process?
    #(-> % process-response f)
    f))


(defn- make-requester
  "Get a prepared clj-http client to make requests to a server."
  [opts]
  (let [all-options (merge (:http-options opts)
                           {:server-port (:port opts)
                            :headers {"OAuth" (-> opts :token name)
                                      :content-type :json
                                      :accept :json}})
        parse-cbs? (:parse-callbacks opts)
        make-url #(format-str "%s/%s/%s/%s" (:host opts) (:api_version opts) (:project opts) %)]
    (fn [method uri & {:keys [payload] {:keys [ok fail]} :callbacks}]
      (let [async? (or (some? ok) (some? fail))
            http-call (if async?
                        #(http-client/request
                          %
                          (wrap-in-process-response parse-cbs? ok)
                          (wrap-in-process-response parse-cbs? fail))
                        #(http-client/request %))
            options (into all-options {:request-method method
                                       :url (make-url uri)
                                       :async? async?
                                       :body (json/generate-string payload)})]
        (if-not async?
          (-> options http-call process-response)
          (http-call options))))))


(defn new-client
  "Creates a new client with a given options.
  Throws exception if can't create one based on given options."
  [config]
  (let [opts (validate-options (deep-merge DEFAULTS (options-from-env) config))
        http (make-requester opts)]
    (->Client http opts)))
