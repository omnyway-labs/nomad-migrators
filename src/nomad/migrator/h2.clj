(ns nomad.migrator.h2
  (:require
   [nomad.core :as nomad :refer [defmigration]]
   [nomad.migrator.core :as migrator]
   [clojure.java.jdbc :as jdbc]))

(defrecord H2Store [db-spec])

(defn connect [{:keys [db]}]
  (H2Store.
   {:subname db
    :subprotocol "h2"
    :classname "org.h2.Driver"}))

(defn init [{:keys [db-spec]}]
  (jdbc/with-connection db-spec
    (jdbc/transaction
     (when (-> (jdbc/with-connection db-spec
                 (jdbc/with-query-results rset
                   [(str "SELECT * FROM information_schema.tables "
                         "WHERE table_schema = 'PUBLIC' "
                         " AND table_name = 'NOMAD_SCHEMA_MIGRATIONS'")]
                   (doall rset)))
               empty?)
       (jdbc/do-commands
        (str "CREATE TABLE nomad_schema_migrations"
             "(tag VARCHAR(256)"
             ",applied TIMESTAMP"
             ",CONSTRAINT uq_tag UNIQUE (tag))"))))))

(extend H2Store
  nomad/IMigrator
  {:-init init
   :-fini migrator/fini
   :-load-migrations migrator/load-migrations
   :-applied? migrator/applied?
   :-apply! migrator/apply!})
