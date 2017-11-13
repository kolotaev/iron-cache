(ns iron-cache.core
  (:refer-clojure :exclude [list get]))

(def ^:const ROOT_URL "cache-aws-us-east-1.iron.io/1")

(def ^:const DEFAULTS
            { :scheme "https"
              :host ROOT_URL
              :port 443
              :api_version 1
              :user_agent "iron_cache_clj"
              :cache_name "default" })


(defprotocol Cache
  "Iron cache instance manipulation"
  (list [this & cbs] "Get list off all cache items")
  (info [this & cbs] "Get information about a cache")
  (delete! [this & cbs] "Delete a cache")
  (clear! [this & cbs] "Clear a cache"))


(defprotocol Key
  "Iron cache instance keys manipulation"
  (put [this key val & cbs] "Add key/value pair to a cache")
  (get [this & cbs] "Get a value stored in a keyfrom a cache")
  (incr [this key & cbs] "Increment value in a cache by a specified key")
  (del [this key & cbs] "Delete a value from a cache by a specified key"))
