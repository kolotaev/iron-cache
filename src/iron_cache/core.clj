(ns iron-cache.core
  (:require [iron-cache.sync])

(def ^:const ROOT_URL "cache-aws-us-east-1.iron.io/1")

(def ^:const default-options
            { :scheme => "https",
              :host => ROOT_URL,
              :port => 443,
              :api_version => 1,
              :user_agent => "iron_cache_clj"
              :cache_name => "default"})


(defprotocol Cache
  "Iron cache instance manipulation"
  (list [this & fns] "Get list off all cache items")
  (info [this & fns] "Get information about a cache")
  (delete! [this & fns] "Delete a cache")
  (clear! [this & fns] "Clear a cache"))


(defprotocol CacheKey
  "Iron cache instance keys manipulation"
  (put [this key val & fns] "Add key/value pair to a cache")
  (get [this & fns] "Get a value stored in a keyfrom a cache")
  (incr [this key & fns] "Increment value in a cache by a specified key")
  (del [this key & fns] "Delete a value from a cache by a specified key"))


(defn new-client
  [options]
  (-> options
      (merge default-options options)
      validate-options
      (SyncClient.)))

(defn new-async-client
  [options])

(defn validate-options
  [opt]))