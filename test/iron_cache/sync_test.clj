(ns iron-cache.sync-test
  (:require [clojure.test :refer :all]
            [iron-cache.core]
            [iron-cache.sync :as ics]))


(deftest create-client
  (let [config {:project "foo" :token "123"}]
    (testing "created client is not nil"
      (is (some? (ics/new-client config))))

    (testing "created client satisfies Cache protocol"
      (is (satisfies? iron-cache.core/Key (ics/new-client config))))

    (testing "created client satisfies Key protocol"
      (is (satisfies? iron-cache.core/Cache (ics/new-client config))))

    (testing "created client has a http-requester"
      (is (function? (:http (ics/new-client config))))))

  (testing "empty config isn't valid"
    (is (thrown? Exception (ics/new-client {})))))

