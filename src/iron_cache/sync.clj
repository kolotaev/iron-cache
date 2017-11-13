(ns iron-cache.sync
  (:require [iron-cache.core :refer :all]
            [clj-http.client :as http]))


(defrecord SyncClient [opts]

;  Cache
;  Key

;  (list [this])
  )


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