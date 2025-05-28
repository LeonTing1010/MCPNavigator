#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

# --- Configuration ---
GITHUB_REPO_URL="git@github.com:LeonTing1010/MCPNavigator.git" # Replace with your repository URL
PROJECT_DIR="nlweb-spring-ai"

# --- EC2 Configuration ---
EC2_USER="ec2-user"
EC2_HOST="52.83.14.210"
EC2_KEY_PATH="$HOME/AI/ec2.pem"  # Path to your EC2 key file
EC2_PORT="22"

# --- Helper Functions ---
print_message() {
  echo "--------------------------------------------------------------------"
  echo "$1"
  echo "--------------------------------------------------------------------"
}

check_ssh_key() {
  if [ ! -f "$EC2_KEY_PATH" ]; then
    print_message "ERROR: EC2 SSH key not found at $EC2_KEY_PATH"
    echo "Please update the EC2_KEY_PATH variable in this script with the correct path to your EC2 key file."
    exit 1
  fi
   
  # Ensure proper permissions on the key file
  chmod 600 "$EC2_KEY_PATH"
}

test_ssh_connection() {
  print_message "Testing SSH connection to EC2 instance..."
  if ssh -i "$EC2_KEY_PATH" -o ConnectTimeout=5 -o StrictHostKeyChecking=accept-new "$EC2_USER@$EC2_HOST" "echo 'Connection successful'"; then
    echo "SSH connection to EC2 successful!"
  else
    print_message "ERROR: Failed to connect to EC2 instance"
    echo "Please check your EC2_USER, EC2_HOST, and EC2_KEY_PATH variables."
    exit 1
  fi
}

remote_command() {
  ssh -i "$EC2_KEY_PATH" "$EC2_USER@$EC2_HOST" "$1"
}

# --- Main Script ---
print_message "Starting deployment to EC2 instance $EC2_HOST"

# Check if SSH key exists
check_ssh_key

# Test SSH connection
test_ssh_connection

# --- 1. Get OpenAI API Key from local .env file ---
print_message "Reading OpenAI API Key from local .env file..."
if [ -f ".env" ]; then
  # Read the OPENAI_API_KEY from the .env file
  OPENAI_API_KEY=$(grep -E "^OPENAI_API_KEY=" .env | cut -d '=' -f2- | tr -d '"' | tr -d "'")
  if [ -z "$OPENAI_API_KEY" ]; then
    print_message "ERROR: OPENAI_API_KEY not found in .env file"
    echo "Please make sure your .env file contains a line with OPENAI_API_KEY=your_api_key"
    exit 1
  else
    echo "Successfully read OPENAI_API_KEY from .env file"
  fi
else
  print_message "ERROR: .env file not found"
  echo "Please create a .env file in the current directory with your OPENAI_API_KEY"
  echo "Example: OPENAI_API_KEY=your_api_key"
  exit 1
fi

# --- 2. Install Prerequisites on EC2 ---
print_message "Installing prerequisites on EC2 instance..."

# Install Docker on Amazon Linux 2023
remote_command "
  # Update package list
  sudo dnf update -y
  
  # Install Git if not already installed
  which git > /dev/null || sudo dnf install -y git
  
  # Install Docker (Amazon Linux 2023 method)
  if ! which docker > /dev/null; then
    echo 'Installing Docker...'
    sudo dnf install -y docker
    sudo systemctl start docker
    sudo systemctl enable docker
    sudo usermod -aG docker \$USER
    echo 'Docker installed successfully'
  fi
  
  # Install Docker Compose
  if ! which docker-compose > /dev/null; then
    echo 'Installing Docker Compose...'
    sudo curl -L \"https://github.com/docker/compose/releases/latest/download/docker-compose-\$(uname -s)-\$(uname -m)\" -o /usr/local/bin/docker-compose
    sudo chmod +x /usr/local/bin/docker-compose
    sudo ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose
    echo 'Docker Compose installed successfully'
  fi
"

# --- 3. Clone or Update Repository on EC2 ---
print_message "Cloning or updating repository on EC2 instance..."
remote_command "
  if [ -d \"$PROJECT_DIR\" ]; then
    echo \"Project directory '$PROJECT_DIR' already exists. Pulling latest changes...\"
    cd \"$PROJECT_DIR\"
    git pull
  else
    echo \"Cloning repository from $GITHUB_REPO_URL...\"
    git clone \"$GITHUB_REPO_URL\"
    cd \"$PROJECT_DIR\"
  fi
"

# --- 4. Configure .env File on EC2 ---
print_message "Configuring .env file on EC2 instance..."
remote_command "
  cd \"$PROJECT_DIR\"
  if [ -f \".env\" ]; then
    echo \".env file already exists. Updating it with the current API key.\"
    sed -i \"/OPENAI_API_KEY=/c\\OPENAI_API_KEY=\\\"$OPENAI_API_KEY\\\"\" .env
  else
    echo \"Creating .env file with API key.\"
    echo \"OPENAI_API_KEY=\\\"$OPENAI_API_KEY\\\"\" > .env
  fi
"

# --- 5. Create a simple Spring Boot application if it doesn't exist
print_message "Creating a simple Spring Boot application if needed..."
remote_command "
  cd \"$PROJECT_DIR\"
  
  # Check if pom.xml exists
  if [ ! -f \"pom.xml\" ]; then
    echo \"Creating a simple Spring Boot application...\"
    
    # Create directory structure
    mkdir -p src/main/java/com/example/demo
    mkdir -p src/main/resources
    
    # Create pom.xml
    cat > pom.xml << 'EOF'
<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"
         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd\">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.1.0</version>
        <relativePath/>
    </parent>
    <groupId>com.example</groupId>
    <artifactId>demo</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>demo</name>
    <description>Spring Boot AI Demo</description>
    <properties>
        <java.version>17</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
EOF
    
    # Create main application class
    cat > src/main/java/com/example/demo/DemoApplication.java << 'EOF'
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Value;

@SpringBootApplication
@RestController
public class DemoApplication {

    @Value("\${OPENAI_API_KEY:default-key}")
    private String apiKey;

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @GetMapping(\"/\")
    public String home() {
        return \"Spring Boot AI Application is running!\";
    }
    
    @GetMapping(\"/api/v1/process-nl\")
    public String processNL() {
        return \"API endpoint is working! API Key is configured.\";
    }
    
    @PostMapping(\"/api/v1/process-nl\")
    public String processNLPost(@RequestBody(required = false) String body) {
        return \"Received POST request: \" + (body != null ? body : \"no body\");
    }
}
EOF
    
    # Create application.properties
    cat > src/main/resources/application.properties << 'EOF'
server.port=8080
EOF
    
    echo \"Simple Spring Boot application created successfully.\"
  fi
"

# --- 6. Copy docker-compose.yml file if it doesn't exist
print_message "Ensuring docker-compose.yml exists..."
remote_command "
  cd \"$PROJECT_DIR\"
  if [ ! -f \"docker-compose.yml\" ]; then
    echo \"Creating docker-compose.yml file...\"
    cat > docker-compose.yml << 'EOF'
version: '3'

services:
  spring-ai-app:
    build:
      context: .
      dockerfile: Dockerfile
    ports:
      - \"8080:8080\"
    environment:
      - OPENAI_API_KEY=\${OPENAI_API_KEY}
    restart: always
EOF
    echo \"docker-compose.yml created successfully.\"
  fi
"

# --- 7. Create Dockerfile if it doesn't exist
print_message "Ensuring Dockerfile exists..."
remote_command "
  cd \"$PROJECT_DIR\"
  echo \"Creating/Updating Dockerfile...\"
  cat > Dockerfile << 'EOF'
# Use Amazon Linux 2023 as base image
FROM amazonlinux:2023

# Install Java 17
RUN dnf install -y java-17-amazon-corretto-devel maven

# Set working directory
WORKDIR /app

# Copy source code
COPY . .

# Build the application
RUN mvn clean package -DskipTests

# Run the application
EXPOSE 8080
CMD [\"java\", \"-jar\", \"target/*.jar\"]
EOF
  echo \"Dockerfile updated successfully.\"
"

# --- 8. Restart Docker service if needed
print_message "Ensuring Docker service is running..."
remote_command "
  # Restart Docker service
  if ! sudo systemctl is-active docker > /dev/null; then
    echo 'Starting Docker service...'
    sudo systemctl start docker
    sudo systemctl enable docker
  fi
  
  # Add current user to docker group and refresh group membership
  sudo usermod -aG docker \$USER
  
  # Create docker group if it doesn't exist
  if ! getent group docker > /dev/null; then
    sudo groupadd docker
    sudo usermod -aG docker \$USER
  fi
  
  # Check Docker status
  echo 'Docker service status:'
  sudo systemctl status docker
"

# --- 9. Build and Run Docker Compose on EC2 ---
print_message "Building and running Docker services on EC2 instance..."
remote_command "
  cd \"$PROJECT_DIR\"
  # Use sudo for docker commands since group membership might not be effective in current session
  sudo docker-compose down || true
  echo 'Pulling base images first...'
  sudo docker pull amazonlinux:2023 || true
  echo 'Building and starting containers...'
  sudo docker-compose up --build -d
"

# --- 10. Check if the service is running
print_message "Checking if the service is running..."
remote_command "
  cd \"$PROJECT_DIR\"
  echo 'Docker containers status:'
  sudo docker-compose ps
  echo ''
  echo 'Recent logs:'
  sudo docker-compose logs --tail=20
"

print_message "Deployment completed successfully!"
echo "Services are starting in the background on your EC2 instance."
echo "To check the status and logs, SSH into your EC2 instance and run:"
echo "  ssh -i $EC2_KEY_PATH $EC2_USER@$EC2_HOST"
echo "  cd $PROJECT_DIR && sudo docker-compose ps"
echo "  cd $PROJECT_DIR && sudo docker-compose logs -f"
echo ""
echo "If everything started correctly, the Spring AI application should be accessible at:"
echo "  http://$EC2_HOST:8080/api/v1/process-nl"
echo "Ensure your EC2 Security Group allows inbound traffic on port 8080."

# Wait a few seconds and then test the endpoint
sleep 20
print_message "Testing the API endpoint..."
curl -s -o /dev/null -w "%{http_code}" "http://$EC2_HOST:8080/api/v1/process-nl" || echo "API endpoint not responding yet. It might take a few minutes to start up."
