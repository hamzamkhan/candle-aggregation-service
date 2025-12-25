FROM dpage/pgadmin4:7.8

# Put the import file in the canonical location pgAdmin reads
COPY servers.json /pgadmin4/servers.json

# Put pgpass where servers.json points
COPY pgpass /pgpass

# Fix perms; pgAdmin/libpq may ignore pgpass if too open
USER root
RUN chmod 644 /pgadmin4/servers.json && chmod 600 /pgpass
USER pgadmin