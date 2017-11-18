(ns iron-cache.core
  (:refer-clojure :exclude [get list])
  (:require [iron-cache.http :refer :all]
            [iron-cache.protocol :refer :all]))


(def ^:const ROOT_URL "cache-aws-us-east-1.iron.io/1")


(def ^:const DEFAULTS
  {:scheme "https"
   :host ROOT_URL
   :port 443
   :api_version 1
   :user_agent "iron_cache_clj"
   :cache_name "default"})


(defrecord Client [opts http]

  Cache

  (list [this & cbs]
    (http :get (format "projects/%s/caches" {:project opts})))

  (info [this cache & cbs]
    (http :get (format "projects/%s/caches/%s" {:project opts} cache)))

  (delete! [this cache & cbs]
    (http :delete (format "projects/%s/caches/%s" {:project opts} cache)))

  (clear! [this cache & cbs]
    (http :post (format "projects/%s/caches/%s/clear" {:project opts} cache)))

  Key

  (get [this cache key & cbs]
    (http :get (format "projects/%s/caches/%s/items/%s" {:project opts} cache key)))

  (put [this cache key val & cbs]
    (http :put (format "projects/%s/caches/%s/items/%s" {:project opts} cache key) val))

  (incr [this cache key val & cbs]
    (http :post (format "projects/%s/caches/%s/items/%s" {:project opts} cache key) {:amount val}))

  (del [this cache key & cbs]
    (http :delete (format "projects/%s/caches/%s/items/%s" {:project opts} cache key))))


(defn- options-from-env
  "Get token and project name out of environment variables."
  []
  {:project (System/getenv "IRON_CACHE_PROJECT")
   :token (System/getenv "IRON_CACHE_TOKEN")})


(defn- validate-options
  "Validates input options. Throws ex if options are inappropriate for client to work."
  [{:keys [project token] :as config}]
  (when-not project
    (throw (ex-info "A project must be specified." {:given config})))
  (when-not token
    (throw (ex-info "An OAuth2 token must be provided." {:given config})))
  config)


(defn new-client
  "Creates a new client with a given options.
  Throws exception if can't create one based on given options."
  [config]
  (let [opts (validate-options (merge DEFAULTS (options-from-env) config))
        http (make-requester opts)]
    (->Client opts http)))
