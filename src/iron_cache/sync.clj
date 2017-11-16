(ns iron-cache.sync
  (:refer-clojure :exclude [get list])
  (:require [iron-cache.core :refer :all]
            [clj-http.client :as http-client]
            [clojure.string :as str]))


(defrecord SyncClient [opts http]

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


(defn- get-http
  "Get a prepared http-client object to make requests to a server."
  [opts]
  (let [url (str (str/replace (:host opts) #"/$" "") ":" (:port opts))
        resource #(format "%s/%s" url %)
        headers {:oauth-token (:token opts)}
        coerce {:as :json}]
    (fn [method uri & payload]
      (case method
        :get    (http-client/get (resource uri) {:headers headers :coerce coerce})
        :delete (http-client/delete (resource uri) {:headers headers :coerce coerce})
        :post   (http-client/post (resource uri) {:headers headers :coerce coerce})
        :put    (http-client/put (resource uri) {:headers headers :coerce coerce})))))


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
    (throw (ex-info "An OAuth2 token must be provided." {:given config}))))


(defn new-client
  "Creates a new client with a given options.
  Throws exception if can't create one based on given options."
  [config]
  (let [opts (validate-options (merge DEFAULTS (options-from-env) config))
        http #()]
    (SyncClient. opts http)))
