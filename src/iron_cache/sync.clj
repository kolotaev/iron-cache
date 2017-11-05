(ns iron-cache.sync)

(:require [clj-http.client :as http])

(defrecord Cache [opts]
  Cache
  CacheKey

  (defn list))