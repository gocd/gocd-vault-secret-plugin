# Vault secret manager plugin
This is a GoCD Secrets plugin which allows users to use [Vault](https://learn.hashicorp.com/vault/) as a secret manger for the GoCD server.

The plugin supports Version 2 of KV Secrets Engine.

## Table of Contents
* [Setup Vault using docker](#setup-vault-using-docker)
* [Configure the plugin](#configure-the-plugin)
* [Building the code base](#building-the-code-base)
* [Troubleshooting](#troubleshooting)

### Setup Vault using docker
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
The plugin needs to be configured with a secret config in order to connect to Vault. The configuration can be added from
the Secrets Management page under Admin > Secret Management.

Alternatively, the configuration can be added directly to the config.xml using the <secretConfig> configuration.

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
          <key>VaultPath</key>
          <value>secret/gocd</value>
        </property>
        <property>
          <key>AuthMethod</key>
          <value>token</value>
        </property>
        <property>
          <key>Token</key>
          <value>some-auth-token</value>
        </property>
      </configuration>
      <rules>
          <allow action="refer" type="environment">env_*</allow>
          <deny action="refer" type="pipeline_group">my_group</deny>
          <allow action="refer" type="pipeline_group">other_group</allow>
      </rules>
    </secretConfig>
</secretConfigs>
```

`<rules>` tag defines where this secretConfig is allowed/denied to be referred. For more details about rules and examples refer the GoCD Secret Management [documentation](https://docs.gocd.org/current/configuration/secrets_management.html)


| Field             | Required | Description                                                     |
| ----------------- |----------| --------------------------------------------------------------- |
| VaultUrl          | Yes      |  The url of the Vault server instance. If no address is explicitly set, the plugin will look to the `VAULT_ADDR` environment variable. |
| VaultPath         | Yes      |  The vault path which holds the secrets as key-value pair (e.g. `secret/gocd`) |
| ConnectionTimeout | No       |  The number of seconds to wait before giving up on establishing an HTTP(s) connection to the Vault server. If no openTimeout is explicitly set, then the object will look to the `VAULT_OPEN_TIMEOUT` environment variable. Defaults to `5 seconds`. | 
| ReadTimeout       | No       |  Once connection has already been established, this is the number of seconds to wait for all data to finish downloading. If no readTimeout is explicitly set, then the object will look to the `VAULT_READ_TIMEOUT` environment variable. Defaults to `30 seconds`. |
| ServerPem         | No       |  An X.509 certificate, in unencrypted PEM format with UTF-8 encoding to use when communicating with Vault over HTTPS |
| AuthMethod        | Yes      |  The auth method to use to authenticate with the Vault server, can be one of `token`, `approle` or `cert` |
| Token             | No       |  Required if using `token` auth method. This is the token used to read secrets from Vault. Ensure this token has a longer ttl, the plugin will not be renewing the token. |
| RoleId            | No       |  Required if using `approle` auth method. The plugins will use the configured `RoleId` and `SecretId` to authenticate with Vault. |
| SecretId          | No       |  Required if using `approle` auth method. |
| ClientKeyPem      | No       |  Required if using `cert` auth method. An RSA private key, in unencrypted PEM format with UTF-8 encoding. |
| ClientPem         | No       |  Required if using `cert` auth method. An X.509 client certificate, in unencrypted PEM format with UTF-8 encoding. |


### Building the code base
To build the jar, run `./gradlew clean test assemble`

## Troubleshooting

### Enable Debug Logs

#### If you are on GoCD version 19.6 and above:

Edit the file `wrapper-properties.conf` on your GoCD server and add the following options. The location of the `wrapper-properties.conf` can be found in the [installation documentation](https://docs.gocd.org/current/installation/installing_go_server.html) of the GoCD server.

```properties
# We recommend that you begin with the index `100` and increment the index for each system property
wrapper.java.additional.100=-Dplugin.com.thoughtworks.gocd.secretmanager.vault.log.level=debug
```

If you're running with GoCD server 19.6 and above on docker using one of the supported GoCD server images, set the environment variable `GOCD_SERVER_JVM_OPTIONS`:

```shell
docker run -e "GOCD_SERVER_JVM_OPTIONS=-Dplugin.com.thoughtworks.gocd.secretmanager.vault.log.level=debug" ...
```

The plugin logs are written to `LOG_DIR/plugin-com.thoughtworks.gocd.secretmanager.vault.log`. The log dir 
- on Linux is `/var/log/go-server`
- on Windows are written to `C:\Program Files\Go Server\logs` 
- on docker images are written to `/godata/logs`
