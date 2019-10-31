#!/bin/sh

PGX_DIR=$1
DIST_DIR="../libs"

ls $PGX_DIR/shared-memory/{client,common,embedded,server,third-party}/* | grep -E "pgx.*(algorithm|pgx|gm-compiler)" | xargs -L1 -I {} cp -rf {} $DIST_DIR

ls $PGX_DIR/shared-lib/{common,embedded,server,third-party}/* | grep -E "pgx.*(algorithm|pgx|gm-compiler)" | xargs -L1 -I {} cp -rf {} $DIST_DIR
