(ns iron-cache.global-test
  (:refer-clojure :exclude [get list])
  (:use [clj-http.fake]
        [iron-cache.response-utils])
  (:require [clojure.test :refer :all]
            [iron-cache.global :refer :all]
            [iron-cache.core :refer [new-client]]))


(defonce client (new-client {:project "amiga" :token "abcd-asdf-qwer"}))


;; Tests ;;

(deftest with-client-macro
  (testing "given a config, uses a config's token"
    (with-fake-routes echo-list
      (with-client {:project "amiga" :token "my-token"}
        (is (= "my-token" (-> (list) :original-request :headers :OAuth))))))

  (testing "given a client instance, uses a client's token"
    (with-fake-routes echo-list
      (with-client client
        (is (= "abcd-asdf-qwer" (-> (list) :original-request :headers :OAuth))))))

  (testing "given a client instance, performs a set of correct requests to list endpoint"
    (with-fake-routes list-200
      (let [items (atom [])
            _ (with-client client
                (swap! items conj (-> (list) first :name))
                (swap! items conj (-> (list) last :name))
                (swap! items conj (-> (list) first :name)))]
        (is (= ["a" "b" "a"] @items)))))

  (testing "given a config, performs correct request to list endpoint"
    (with-fake-routes list-200
      (with-client {:project "amiga" :token "my-token"}
        (is (= "b" (-> (list) last :name))))))

  (testing "given a config, performs correct request to info endpoint"
    (with-fake-routes info-200
      (with-client client
        (is (= 80000 (-> (info "credit-cards") :size))))))

  (testing "given a config, performs correct request to delete-cache endpoint"
    (with-fake-routes delete-cache
      (with-client client
        (is (= "Deleted" (-> (delete! "credit-cards") :msg))))))

  (testing "given a config, performs correct request to clear-cache endpoint"
    (with-fake-routes clear-cache
      (with-client client
        (is (= "Cleared." (-> (clear! "credit-cards") :msg)))))))


(deftest global-client
  (testing "given a config, uses a config's token"
    (init-client! {:project "amiga" :token "my-token"})
    (with-fake-routes echo-list
      (is (= "my-token" (-> (list) :original-request :headers :OAuth))))
    (init-client! {:project "amiga" :token "foo-bar"})
    (with-fake-routes echo-list
      (is (= "foo-bar" (-> (list) :original-request :headers :OAuth)))))

  (testing "performs a correct request to get-key endpoint"
    (init-client! {:project "amiga" :token "my-token"})
    (with-fake-routes get-key-200
      (let [resp (get "users" :john)]
        (is (map? resp))
        (is (= "john" (-> resp :key)))
        (is (= 25 (-> resp :value :age)))
        (is (= 12345 (-> resp :cas))))))

  (testing "performs a correct request to incr-key endpoint"
    (init-client! {:project "amiga" :token "my-token"})
    (with-fake-routes incr-key-201
      (let [resp (incr :credit-cards :1234 10)]
        (is (map? resp))
        (is (= "Added" (:msg resp)))
        (is (= 100 (:value resp))))))

  (testing "performs a correct request to put-key endpoint"
    (init-client! {:project "amiga" :token "my-token"})
    (with-fake-routes put-key-201-minimal
      (let [resp (put :credit-cards 1234 {:value 85})]
        (is (map? resp))
        (is (= "Stored." (:msg resp))))))

  (testing "performs a correct request to del-key endpoint"
    (init-client! {:project "amiga" :token "my-token"})
    (with-fake-routes delete-key
      (let [resp (del :credit-cards 1234)]
        (is (map? resp))
        (is (= "Deleted" (:msg resp)))))))
