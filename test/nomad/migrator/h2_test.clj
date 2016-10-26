(ns nomad.migrator.h2-test
  (:require
   [clojure.test :refer :all]
   [clojure.java.jdbc :as jdbc]
   [nomad.core :as nomad]
   [nomad.migrator.h2 :as h2]))

(defonce db (h2/connect {:db "mem:test;DB_CLOSE_DELAY=-1;IGNORECASE=TRUE"}))

(deftest migrations
  (is (= {:index #{}, :clauses []} (nomad/clear-migrations!)))
  (is (= :ok
         (nomad/register-migration! "init-schema"
                                    {:up (fn []
                                           (jdbc/do-commands
                                            "CREATE TABLE test1(name VARCHAR(32))"))})))
  (is (= :ok 
         (nomad/register-migration! "add-test1-age"
                                    {:up (fn []
                                           (jdbc/do-commands
                                            "ALTER TABLE test1 ADD COLUMN age INTEGER"))})))
  (is (= :ok (nomad/migrate! db)))

  (is (= :ok
         (do
           (jdbc/with-connection (:db-spec db)
             (jdbc/insert-record :test1 {:name "foo" :age 42}))
           :ok)))

  (is (< 0
         (jdbc/with-connection (:db-spec db)
           (jdbc/with-query-results rset
             ["SELECT COUNT(1) AS COUNT FROM test1"]
             (-> rset doall first :count)))))
  )
