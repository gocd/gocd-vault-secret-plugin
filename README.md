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

#### Vault Configuration

| Field                       | Required | Description                                                                                                                                                                                                                                                        |
|-----------------------------|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| VaultUrl                    | Yes      | The url of the Vault server instance. If no address is explicitly set, the plugin will look to the `VAULT_ADDR` environment variable.                                                                                                                              |
| ConnectionTimeout           | No       | The number of seconds to wait before giving up on establishing an HTTP(s) connection to the Vault server. If no openTimeout is explicitly set, then the object will look to the `VAULT_OPEN_TIMEOUT` environment variable. Defaults to `5 seconds`.                |
| ReadTimeout                 | No       | Once connection has already been established, this is the number of seconds to wait for all data to finish downloading. If no readTimeout is explicitly set, then the object will look to the `VAULT_READ_TIMEOUT` environment variable. Defaults to `30 seconds`. |
| ServerPem                   | No       | An X.509 certificate, in unencrypted PEM format with UTF-8 encoding to use when communicating with Vault over HTTPS                                                                                                                                                |
| Max Retries                 | No       | Number of times to attempt to gather secrets from Vault. Defaults to `0`.                                                                                                                                                                                          |
| Retry Interval Milliseconds | No       | Duration between retry attempts (set by `Max Retries`). Defaults to `100 milliseconds`.                                                                                                                                                                            |

#### Authentication

| Field                       | Required | Description                                                                                                                                                                                                                                                        |
|-----------------------------|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| AuthMethod                  | Yes      | The auth method to use to authenticate with the Vault server, can be one of `token`, `approle` or `cert`                                                                                                                                                           |
| Token                       | No       | Required if using `token` auth method. This is the token used to read secrets from Vault. Ensure this token has a longer ttl, the plugin will not be renewing the token.                                                                                           |
| RoleId                      | No       | Required if using `approle` auth method. The plugins will use the configured `RoleId` and `SecretId` to authenticate with Vault.                                                                                                                                   |
| SecretId                    | No       | Required if using `approle` auth method.                                                                                                                                                                                                                           |
| ClientKeyPem                | No       | Required if using `cert` auth method. An RSA private key, in unencrypted PEM format with UTF-8 encoding.                                                                                                                                                           |
| ClientPem                   | No       | Required if using `cert` auth method. An X.509 client certificate, in unencrypted PEM format with UTF-8 encoding.                                                                                                                                                  |

#### Secret Engines

To configure secret engines, set the value `SecretEngine` to either `secret` for the key-value secret storage or to `oidc` to use Vault with GoCD with pipeline identity tokens.

##### Key-Value Secret Engine

| Field                       | Required | Description                                                                                                                                                                                                                                                        |
|-----------------------------|----------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SecretEngine                | No       | This defines the [secret engine type](https://www.vaultproject.io/docs/secrets). Either `secert` or `oidc`. Defaults to `secret`.                                                                                                                                  |
| VaultPath                   | Yes      | The vault path which holds the secrets as key-value pair (e.g. `secret/gocd`)                                                                                                                                                                                      |

##### OIDC Provider

| Field                        | Required | Description                                                                                                                                                                                                                              |
|------------------------------|----------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| SecretEngine                 | Yes      | This defines the [secret engine type](https://www.vaultproject.io/docs/secrets). Either `secert` or `oidc`. Defaults to `secret`.                                                                                                        |
| VaultPath                    | Yes      | Path which returns the OIDC token from Vault. Usually this starts with `/v1/identity/oidc/token/...`                                                                                                                                     |
| PipelineTokenAuthBackendRole | Yes      | The Token-Auth Backend Role which is used by this plugin to assume a certain pipeline entity.                                                                                                                                            |
| PipelinePolicy               | No       | An comma separated list of optional [pipeline policy names](https://www.vaultproject.io/api-docs/auth/token#policies) to restrict the permissions this assumed pipeline entity will have.                                                |
| CustomEntityNamePrefix       | No       | Use this optional parameter to namespace your CI environments. When specified each pipeline entity created in Vault will have the syntax `<code>`{CustomEntityNamePrefix}-{PipelineName}`</code>`. Default is `<code>`pipeline-identity` |
| GoCDServerUrl                | Yes      | GoCD server base URL to issue API calls.                                                                                                                                                                                                 |
| GoCDUsername                 | Yes      | GoCD username which will be used by this plugin to authenticate against the GoCD API.                                                                                                                                                    |
| GoCDPassword                 | Yes      | GoCD credentials which will be used by this plugin to authenticate against the GoCD API.                                                                                                                                                 |

### OpenID Connect

The GoCD Vault Secret Plugin can provide the calling pipeline with an own pipeline identity token, issued by Vault. It does so
by fetching GoCD API to retrieve information about the pipeline and then to create an entity in Vault which contains
these pipeline information as metadata. Finally, this newly created entity is used to create an OIDC Identity token (JWT) which
is signed by Vault and contains the following pipeline information:

| Field        | Description                                                                                                    |
|--------------|----------------------------------------------------------------------------------------------------------------|
| pipeline     | Pipeline name.                                                                                                 |
| group        | Pipeline group.                                                                                                |
| organization | Github organization or owning user account name (Other git servers are currently not supported).               |
| repository   | Repository name.                                                                                               |
| branch       | (Optional) Can be null. The current git branch. This is null if the SCM material is provided by a GoCD plugin. |

Example OIDC Identity Token Body:

```json
{
  "aud": "https://some.gocd.domain.com",
  "branch": "main",
  "exp": 1648541403,
  "group": "defaultGroup",
  "iat": 1648455003,
  "iss": "https://some.vault.domain.com/v1/identity/oidc",
  "namespace": "root",
  "organization": "anroc",
  "pipeline": "deploy-gocd-vault-plugin",
  "repository": "gocd-vault-secret-plugin",
  "sub": "52349e30-cff8-7959-b61c-e9f9280d6233"
}
```

Identity tokens can be used to authenticate to Vault to use Vaults rich API to fetch more then just static key-value secrets,
as well as authenticating to other services such as [Google Cloud Project](https://cloud.google.com/iam/docs/workload-identity-federation).

##### Claim building

As GoCD supports multiple material configuration, it highly depends on the order of the multiple definition as well as 
the type of material configured. The supported cases are defined below.

1. If multiple materials are defined, the first Git or Git Plugin material will be used.
2. If only pipeline dependency materials are defined, the first dependency will be used to resolve the referenced configuration.

Gor Git plugin materials, no branch information can be fetched and the branch claim remains empty.

#### Vault Configuration

To enable the Vault OIDC Provider follow the [Vault OIDC Provider documentation](https://www.vaultproject.io/docs/concepts/oidc-provider#oidc-provider).
The plugin will create for each new pipeline a new [entity](https://www.vaultproject.io/api-docs/secret/identity/entity) in vault with respective pipeline metadata
(requires `read`, `write` and `update` permission to `/identity/entity/*`; policy should be restricted to entity name and attached policy name).  
These metadata can be attached to the scopes of the OIDC Token using this template as an example:

```plain
  {
    "pipeline":     {{identity.entity.metadata.pipeline}},
    "group":        {{identity.entity.metadata.group}},
    "repository":   {{identity.entity.metadata.repository}},
    "organization": {{identity.entity.metadata.organization}},
    "branch":       {{identity.entity.metadata.branch}}
  }
```

This plugin needs to assume the newly crated pipeline entity in order to retrieve a pipeline identity token. For that the plugin
logs in as a pipeline via the token authentication endpoint. In order to do so, a [token auth backend role](https://www.vaultproject.io/api-docs/auth/token#create-update-token-role) 
needs to be created in Vault, which is bound to a policy that allows to retrieve an OIDC Identity token. 

Example terraform definition for this auth token backend role:
```hcl
resource "vault_policy" "<PIPELINE_POLICY_NAME>" {
  name   = "<PIPELINE_POLICY_NAME>"
  policy = <<EOT
    path "identity/oidc/token/<OIDC_TOKEN_ENDPOINT_NAME>" {
      capabilities = ["read"]
    }
  EOT
}

resource "vault_token_auth_backend_role" "<AUTH_TOKEN_BACKEND_ROLE_NAME>" {
  role_name              = "<AUTH_TOKEN_BACKEND_ROLE_NAME>"
  allowed_policies       = [vault_policy.<PIPELINE_POLICY_NAME>.name]
  allowed_entity_aliases = ["<CUSTOM_ENTITY_NAME_PREFIX->entity-alias-*"]
}
```

In addition to the pipeline entity the plugin will also create an [entity alias](https://www.vaultproject.io/api-docs/secret/identity/entity-alias) 
(requires `create` and `update` permission to `/identity/entity-alias`) bound to the newly created entity,
as well as the token authentication endpoint (requires `read` permission to the `/sys/auth` path). 
To assume a certain pipeline, a new vault token is created (requires `update` permission to `auth/token/create/<AUTH_TOKEN_BACKEND_ROLE_NAME>`).
Using the new Vault token a request to fetch the OIDC Identity Token is done and returned to the pipeline. 


#### Required Vault Policies

**Required policy for the vault plugin**

```hcl
# Used to read token accessor
path "sys/auth" {
  capabilities = ["read"]
}

# Assume identity of pipeline 
path "auth/token/create/<AUTH_TOKEN_BACKEND_ROLE_NAME>" {
  capabilities = ["update"]
}

# Create entity alias for pipelines
path "identity/entity-alias" {
  capabilities = ["create", "update"]
  allowed_parameters = {
    "mount_accessor" = ["auth_token_*"]
    "name" = ["<CUSTOM_ENTITY_PREFIX-><PIPELINE_NAME>-entity-alias-*"]
    "*" = []
  }
}

# Create entity for pipelines
path "identity/entity/*" {
  capabilities = ["create", "update", "read"]
  allowed_parameters = {
    "name" = ["<CUSTOM_ENTITY_PREFIX>-*"]
    "policies" = ["<PIPELINE_POLICIES_LIST>"]
    "*" = []
  }
}
```

**Required policy for a pipeline:**

```hcl
path "identity/oidc/token/<VAULT_OIDC_IDENTITY_ROLE_NAME>" {
    capabilities = ["read"]
}
```

#### Usage

To use this plugin add this SECRET reference to your pipeline configuration:
```plain
IDENTITY_TOKEN={{SECRET:[<VAULT_PLUGIN_ID>][<PIPELINE_NAME>]}}
```

The pipeline name as a secret key is important for the plugin to know which pipeline identity token should be returned. 
This value is trusted by the plugin and should be set by a trusted party in order to prevent pipeline privilege escalation.

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
