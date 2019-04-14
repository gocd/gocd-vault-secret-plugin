# Vault secret manager plugin
The plugin allows users to use [Vault](https://learn.hashicorp.com/vault/) as a secret manger for the GoCD server.

### Setup vault
1. Run following command to start docker container for vault
```bash
docker run --cap-add=IPC_LOCK -e VAULT_DEV_ROOT_TOKEN_ID=some-token -p8200:8200  -d --name=dev-vault vault:latest
```

2. Once container is up and running exec to container in order to create secrets

```bash
export VAULT_ADDR='http://127.0.0.1:8200'
export VAULT_TOKEN='some-token'
```

The above environment variables are used by `vault` client to connect to the vault server.

3. Create secret

```bash
export BASE_PATH='secret/gocd'
vault kv put $BASE_PATH AWS_ACCESS_KEY=ABDASDKDLKM \
    AWS_SECRET_KEY=asdsfksfhkdfgfhfghhg; \
    GITHUB_TOKEN=97dasdd9789sd79sdf7sd9f7s9f9sd7f9sdvfd9gd9
```

### Configure the plugin
The plugin requires secret config in order to connect with vault -

```xml
<secretConfigs>
    <secretConfig id="vault" pluginId="com.thoughtworks.gocd.secretmanager.vault">
      <description>All secrets for env1</description>
      <configuration>
        <property>
          <key>VaultUrl</key>
          <value>http://127.0.0.1:8200</value>
        </property>
        <property>
          <key>token</key>
          <value>some-token</value>
        </property>
        <property>
          <key>VaultKey</key>
          <value>secret/gocd</value>
        </property>
      </configuration>
    </secretConfig>
</secretConfigs>
```

| Field             | Description                                                     |
| ----------------- | --------------------------------------------------------------- |
| VaultUrl          |  The url of the Vault server instance to which API calls should be sent. If no address is explicitly set, the object will look to the `VAULT_ADDR` environment variable. | 
| Token             |  The token used to access Vault. If no token is explicitly set, then the object will look to the `VAULT_TOKEN` environment variable.| 
| VaultKey          |  The vault key value from which to read (e.g. `secret/gocd`) | 
| ConnectionTimeout |  The number of seconds to wait before giving up on establishing an HTTP(s) connection to the Vault server. If no openTimeout is explicitly set, then the object will look to the `VAULT_OPEN_TIMEOUT` environment variable. Defaults to `5 seconds`. | 
| ReadTimeout       |  Once connection has already been established, this is the number of seconds to wait for all data to finish downloading. If no readTimeout is explicitly set, then the object will look to the `VAULT_READ_TIMEOUT` environment variable. Defaults to `30 seconds`. | 
| ClientKeyPem      |  An RSA private key, in unencrypted PEM format with UTF-8 encoding. | 
| ClientPem         |  An X.509 client certificate, in unencrypted PEM format with UTF-8 encoding. | 
| ServerPem         |  An X.509 certificate, in unencrypted PEM format with UTF-8 encoding. | 


### Building the code base
To build the jar, run `./gradlew clean test assemble`

### Troubleshooting

To enable debug log add following environment variable - 

```
GO_SERVER_SYSTEM_PROPERTIES=-Dplugin.com.thoughtworks.gocd.secretmanager.vault.log.level=debug
``` 

The plugin logs are written to `LOG_DIR/plugin-com.thoughtworks.gocd.secretmanager.vault.log`. The log dir 
- on Linux is `/var/log/go-server`
- on Windows are written to `C:\Program Files\Go Server\logs` 
- on docker images are written to `/godata/logs`
