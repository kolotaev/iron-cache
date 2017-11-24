(ns iron-cache.core-test
  (:require [clojure.test :refer :all]
            [iron-cache.core :as ic]))


(defn- map-subset? [a b]
  "Helper function to test if map is a subset of another map"
  (every? (fn [[k _ :as entry]] (= entry (find b k))) a))


(deftest new-client
  (let [config {:project "foo" :token "123"}]
    (testing "created client is not nil"
      (is (some? (ic/new-client config))))

    (testing "created client satisfies Cache protocol"
      (is (satisfies? ic/Cache (ic/new-client config))))

    (testing "created client satisfies Key protocol"
      (is (satisfies? ic/Key (ic/new-client config))))

    (testing "created client has a http-requester function"
      (is (function? (:http (ic/new-client config)))))))


(deftest validate-options
  (testing "empty config isn't valid"
    (is (thrown? Exception (ic/new-client {}))))

  (testing "specifying only project isn't a valid config"
    (is (thrown-with-msg? Exception #"token.*provided" (ic/new-client {:project "foo"}))))

  (testing "specifying only OAuth token isn't a valid config"
    (is (thrown-with-msg? Exception #"project.*specified" (ic/new-client {:token "123-qwerty"})))))


(deftest options-from-env
  (with-redefs [ic/env (constantly "foo")]
    (testing "missing project and token are taken from env variables"
      (is (map-subset? {:project "foo" :token "foo"} (-> {} ic/new-client :config))))

    (testing "missing token is taken from env "
      (is (map-subset? {:project "a" :token "foo"} (-> {:project "a"} ic/new-client :config))))

    (testing "missing project is taken from env "
      (is (map-subset? {:project "foo" :token "b"} (-> {:token "b"} ic/new-client :config))))

    (testing "env values do not override provided project and token"
      (is (map-subset? {:project "a" :token "b"} (-> {:token "b" :project "a"} ic/new-client :config))))))


(deftest options-merge
  (with-redefs [ic/env (constantly "x")]
    (testing "base custom param overrides defaults"
      (is (= {:host "https://cache-aws-us-east-1.iron.io"
              :port 443
              :api_version 2
              :http-options {:client-params {"http.useragent" "iron_cache_clj_client"}
                             :content-type :json
                             :accept :json
                             :as :json
                             :throw-exceptions false
                             :coerce :always}
              :project "x"
              :token "x"}
             (-> {:api_version 2} ic/new-client :config))))

    (testing "several base custom params override defaults"
      (is (= {:host "some-host"
              :port 443
              :api_version 3
              :http-options {:client-params {"http.useragent" "iron_cache_clj_client"}
                             :content-type :json
                             :accept :json
                             :as :json
                             :throw-exceptions false
                             :coerce :always}
              :project "x"
              :token "x"}
             (-> {:api_version 3 :host "some-host"} ic/new-client :config))))

    (testing "several base and some custom http-options override defaults"
      (is (= {:host "https://cache-aws-us-east-1.iron.io"
              :port 443
              :api_version 3
              :http-options {:client-params {"foo" "bar", "http.useragent" "iron_cache_clj_client"}
                             :content-type :json
                             :accept :transit
                             :as :json
                             :throw-exceptions false
                             :coerce :always}
              :project "x"
              :token "x"}
             (-> {:api_version 3
                  :http-options {:client-params {"foo" "bar"}
                                 :accept :transit}}
                 ic/new-client :config))))

    (testing "several base and some custom http-options with token specified override defaults and env"
      (is (= {:host "https://cache-aws-us-east-1.iron.io"
              :port 443
              :api_version 3
              :http-options {:client-params {"foo" "bar", "http.useragent" "clj"}
                             :content-type :json
                             :accept :transit
                             :as :json
                             :throw-exceptions false
                             :coerce :always}
              :project "x"
              :token "abc"}
             (-> {:api_version 3
                  :token "abc"
                  :http-options {:coerce :always
                                 :client-params {"foo" "bar", "http.useragent" "clj"}
                                 :accept :transit}}
                 ic/new-client :config))))))
