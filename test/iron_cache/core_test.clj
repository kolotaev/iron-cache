(ns iron-cache.core-test
  (:refer-clojure :exclude [list get])
  (:require [clojure.test :refer :all]
            [iron-cache.core :refer :all]
            [iron-cache.protocol :refer :all]))


(deftest create-client
  (let [config {:project "foo" :token "123"}]
    (testing "created client is not nil"
      (is (some? (new-client config))))

    (testing "created client satisfies Cache protocol"
      (is (satisfies? Cache (new-client config))))

    (testing "created client satisfies Key protocol"
      (is (satisfies? Key (new-client config))))

    (testing "created client has a http-requester"
      (is (function? (:http (new-client config))))))

  (testing "empty config isn't valid"
    (is (thrown? Exception (new-client {})))))

