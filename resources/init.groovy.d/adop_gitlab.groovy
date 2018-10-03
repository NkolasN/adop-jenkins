import jenkins.model.*;
import com.dabsquared.gitlabjenkins.connection.*;

// Check if enabled
def env = System.getenv()
if (!env['ADOP_GITLAB_ENABLED'].toBoolean()) {
    println "--> ADOP Gitlab Disabled"
    return
}

// Variables
def gitlab_host_name = env['GITLAB_HOST_NAME']
def gitlab_api_token = env['GITLAB_API_TOKEN']
def gitlab_ignore_cert_errors = env['GITLAB_IGNORE_CERTIFICATE_ERRORS'] ?: false
def gitlab_connection_timeout = env['GITLAB_CONNECTION_TIMEOUT'] ?: 10
def gitlab_read_timeout = env['GITLAB_READ_TIMEOUT'] ?: 10

// Constants
def instance = Jenkins.getInstance()

Thread.start {
  sleep 10000

  // Gitlab
  println "--> Configuring Gitlab"
  def gitlab_config = instance.getDescriptor("com.dabsquared.gitlabjenkins.connection.GitLabConnectionConfig")
  
  def gitlab_conn = new GitLabConnection(
    'ADOP Gitlab',
    gitlab_host_name,
    gitlab_api_token,
    gitlab_ignore_cert_errors,
    gitlab_connection_timeout,
    gitlab_read_timeout
  )

  def gitlab_connections = gitlab_config.getConnections()

  def gitlab_server_exists = false
  gitlab_connections.each {
	connection_name = (GitLabConnection) it
	if ( gitlab_conn.name == connection_name.getName() ) {
	  gitlab_server_exists = true
	  println("Found existing installation: " + gitlab_conn.name)
	}
  }
  
  if (!gitlab_server_exists) {
	gitlab_connections.add(gitlab_conn)
    gitlab_config.setConnections(gitlab_connections)
	gitlab_config.save()
  }

  // Save the state
  instance.save()
}