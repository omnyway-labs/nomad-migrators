(ns nomad.migrator.postgres-test
  (:require
   [clojure.test :refer :all]
   [clojure.java.shell :as sh]
   [clojure.java.jdbc :as jdbc]
   [nomad.core :as nomad]
   [nomad.migrator.postgres :as postgres]))

(def db (atom nil))

(defn fixture [f]
  (let [db-name (str "nomad-test-" (gensym))]
    (try
      (sh/sh "dropdb" db-name)
      (sh/sh "createdb" db-name)
      (reset! db (postgres/connect {:db db-name
                                    :user "postgres"
                                    :password "postgres"
                                    :host "localhost"
                                    :post 5432}))
      (f)
      (finally
        (sh/sh "dropdb" db-name)))))

(use-fixtures :each fixture)

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
  (is (= :ok (nomad/migrate! @db)))

  (is (= :ok
         (do
           (jdbc/with-connection (:db-spec @db)
             (jdbc/insert-record :test1 {:name "foo" :age 42}))
           :ok)))

  (is (< 0
         (jdbc/with-connection (:db-spec @db)
           (jdbc/with-query-results rset
             ["SELECT COUNT(1) AS COUNT FROM test1"]
             (-> rset doall first :count)))))
  )
