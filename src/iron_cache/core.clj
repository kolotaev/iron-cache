(ns iron-cache.core
  (:refer-clojure :exclude [get list])
  (:require [iron-cache.protocol :refer :all]
            [clj-http.client :as http-client]))

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
                  :coerce {:as :json}}})


;;; Clent record ;;;

(defrecord Client [http config]

  Cache

  (list [this & cbs]
    (http :get (format "caches")))

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


(defn- make-requester
  "Get a prepared clj http-client to make requests to a server."
  [opts]
  (let [all-options (merge (:http-options opts)
                           {:scheme (:scheme opts)
                            :server-name (:host opts)
                            :server-port (:port opts)
                            :headers {:oauth-token (:token opts), :content-type :json, :accept :json}})
        make-uri #(format "%s/%s/%s/" (:api_version opts) (:project opts) %)]
    (fn [method uri & [payload cbs]]
      (-> all-options
          into {:request-method method
                :uri (make-uri uri)
                :async? (map? (or cbs nil))
                :body payload}
          http-client/request))))


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