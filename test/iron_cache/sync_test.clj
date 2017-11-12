(ns iron-cache.sync-test
  (:require [clojure.test :refer :all]
            [iron-cache.core]
            [iron-cache.sync :as ics]))


(deftest create-client
  (testing "created client is not nil"
    (is (some? (ics/new-client {}))))

  (testing "created client satisfies Cache protocol"
    (is (satisfies? iron-cache.core/Key (ics/new-client {}))))

  (testing "created client satisfies Key protocol"
    (is (satisfies? iron-cache.core/Cache (ics/new-client {})))))


(deftest validate-options
  (testing "empty config isn't valid"
    (throws-excpeption Exception. (ics/validate-options {}))))
