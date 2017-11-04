(ns iron-cache.core)

(def ^:const ROOT_URL "cache-aws-us-east-1.iron.io")

(def ^:const default_options
            { :scheme => "https",
              :host => ROOT_URL,
              :port => 443,
              :api_version => 1,
              :user_agent => "iron_cache_clj"
              :cache_name => "default"})


(defprotocol Cache
  "Iron cache instance"
  (defn list [this & fns] "Get list off all cache items")
  (defn info [this & fns] "Get information about a cache")
  (defn delete [this & fns] "Delete a cache")
  (defn clear [this & fns] "Clear a cache"))


(defn new-client
  [options]
  )

(defn new-async-client
  [options])