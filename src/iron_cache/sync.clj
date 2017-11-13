(ns iron-cache.sync
  (:require [iron-cache.core :refer :all]
            [clj-http.client :as http-client]
            [clojure.string :as str]))


(defrecord SyncClient [opts]

  Cache
;  Key

  (list [this & cbs]
    (let [url (format "projects/%s/caches" {:project opts})]
      )))


(defn- http
  "Get a prepared http-client object to make requests to a server."
  [opts]
  (fn [method uri]
    (http-client method
      (format "%s:%/%s" (str/replace {:host opts} #"/$" "") {:port opts} uri)
      {})))


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
  [options]
  (-> options
    (merge DEFAULTS (options-from-env) options)
    validate-options
    ->SyncClient))