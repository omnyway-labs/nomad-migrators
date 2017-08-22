#!/bin/sh

git clone https://${READ_BOT_TOKEN}@github.com/omnypay/omnypay-ci.git  ~/omnypay-ci --depth 1 --quiet

~/omnypay-ci/deps.sh
apt-get install -yy postgresql-client-common  postgresql-client-9.6
