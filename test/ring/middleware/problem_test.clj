(ns ring.middleware.problem-test
  (:require [clojure.test :refer :all]
            [ring.middleware.problem :refer :all]))

(defn problem!
  "Utility function for catching exceptions an building
  a problem."
  [exception]
  (try
    (throw exception)
    (catch java.lang.RuntimeException ex
      (problem ex))))

(deftest make-problem-test
  (testing "two argument specs uniquely identify a problem"
    (let [npe (make-problem java.lang.NullPointerException
                            {:type "pointer-bug"
                             :title "Pointer Bug."
                             :status 501})
          npp (problem! (NullPointerException.))]
      (is (= (:type npp) "pointer-bug"))
      (is (some? (:instance npp)))))
  (testing "clojure's ex-info maps trump defaults"
    (let [jre (make-problem java.lang.RuntimeException
                            {:type "basic-problem"
                             :title "Basic Problem"
                             :status 500})
          umm (problem! (ex-info "Ahoy!" {:status 404 :detail "Mate!"}))]
      (is (= (:status umm) 404))
      (is (= (:detail umm) "Mate!"))
      (is (instance? java.util.UUID (:instance umm)))))
  (testing "three argmument specs skip default behavior"
    (let [npe (make-problem java.lang.NullPointerException
                            {:type "pointer-bug"
                             :title "Pointer Bug."
                             :status 500}
                            (fn [p _] p))
          npp (problem! (NullPointerException.))]
      (is (= (:type npp) "pointer-bug"))
      (is (nil? (:instance npp))))))

(deftest make-problems-test
  (testing "making multiple problems"
    (let [it (make-problems [[java.lang.NullPointerException
                              {:status 500 :type "npe-problem"}]
                             [java.lang.UnsupportedOperationException
                              {:status 501 :type "uoe-problem"}]
                             [java.lang.RuntimeException
                              {:status 418 :type "wtf-problem"}]])]
      (is (= 3 (count it)))
      (is (extends? Problematic java.lang.NullPointerException))
      (is (extends? Problematic java.lang.UnsupportedOperationException))
      (is (extends? Problematic java.lang.RuntimeException)))))

(deftest defproblems-test
  (testing "defining a seet of problems"
    (do (defproblems test-problems
          [[java.lang.NullPointerException
            {:status 500 :type "npe-problem"}]
           [java.lang.UnsupportedOperationException
            {:status 501 :type "uoe-problem"}]
           [java.lang.RuntimeException
            {:status 418 :type "wtf-problem"}]])
        (is (= 3 (count test-problems)))
        (is (extends? Problematic java.lang.NullPointerException))
        (is (extends? Problematic java.lang.UnsupportedOperationException))
        (is (extends? Problematic java.lang.RuntimeException)))))

(deftest make-instance-test
  "TODO")

(deftest wrap-problem-test
  "TODO")

(deftest as-json-test
  "TODO")
