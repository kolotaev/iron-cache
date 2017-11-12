(ns iron-cache.sync
  (:require [iron-cache.core :refer :all]
            [clj-http.client :as http]))


(defrecord SyncClient [opts]

  Cache
  Key

;  (list [this])
  )


(defn new-client
  [options]
  (-> options
    (merge default-options options)
    validate-options
    SyncClient.))

(defn- validate-options
  [{:keys [] :as options}]
  "Validates input options. Throws ex if options are inappropriate for client to work."
  )