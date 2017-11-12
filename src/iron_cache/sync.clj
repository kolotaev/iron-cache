(ns iron-cache.sync
  (:require [iron-cache.core :refer :all]
            [clj-http.client :as http]))


(defrecord SyncClient [opts]

;  Cache
;  Key

;  (list [this])
  )


(defn new-client
  [options]
  (-> options
    (merge DEFAULTS options options-from-env)
    validate-options
    ->SyncClient))


(defn- options-from-env
  []
  {:project (System/getenv "IRON_CACHE_PROJECT")
   :token (System/getenv "IRON_CACHE_TOKEN")})


(defn- validate-options
  [{:keys [project token] :as config}]
  "Validates input options. Throws ex if options are inappropriate for client to work."
  (when-not project
    (throw (ex-info "A project must be specified." {:given config})))
  (when-not token
    (throw (ex-info "An OAuth2 token must be provided." {:given config}))))
