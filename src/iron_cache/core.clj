(ns iron-cache.core)

(def ^:const ROOT_URL "cache-aws-us-east-1.iron.io")

(def ^:const default_options
            { :scheme => "https",
              :host => ROOT_URL,
              :port => 443,
              :api_version => 1,
              :user_agent => "iron_cache_clj"
              :cache_name => "default"})

(defn new-client
  [options])

(defn new-async-client
  [options])