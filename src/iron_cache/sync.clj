(ns iron-cache.sync
  (:require [iron-cache.core :refer :all]
            [clj-http.client :as http]))


(defrecord SyncClient [opts]

  Cache
  CacheKey

  (list [this]))


(defn new-client
  [options]
  (-> options
    (merge default-options options)
    validate-options
    (SyncClient.)))