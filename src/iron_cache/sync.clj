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
    (merge DEFAULTS options)
    validate-options
    ->SyncClient))

(defn- validate-options
  [{:keys [project token] :as config}]
  "Validates input options. Throws ex if options are inappropriate for client to work."
  (when-not project
    (throw (ex-info "A project must be specified." config)))
  (when-not token
    (throw (ex-info "An OAuth2 token must be provided." config))))
