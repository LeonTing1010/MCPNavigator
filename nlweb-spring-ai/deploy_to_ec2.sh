#!/bin/bash

# Exit immediately if a command exits with a non-zero status.
set -e

# --- Configuration ---
GITHUB_REPO_URL="<YOUR_GITHUB_REPO_URL_HERE>" # Replace with your repository URL
PROJECT_DIR="nlweb-spring-ai"
OPENAI_API_KEY_PLACEHOLDER="YOUR_OPENAI_API_KEY_HERE" # For the .env file

# --- Helper Functions ---
print_message() {
  echo "--------------------------------------------------------------------"
  echo "$1"
  echo "--------------------------------------------------------------------"
}

# --- 1. Prerequisites Installation (Optional - Uncomment if needed) ---
# Ensure your EC2 instance has Git, Docker, and Docker Compose installed.
# These commands are examples and might need adjustment for your specific Linux distribution.

# print_message "Updating package list..."
# sudo apt-get update -y

# print_message "Installing Git..."
# sudo apt-get install -y git

# print_message "Installing Docker..."
# curl -fsSL https://get.docker.com -o get-docker.sh
# sudo sh get-docker.sh
# sudo usermod -aG docker $(whoami) # Add current user to docker group (log out and log back in to apply)
# rm get-docker.sh

# print_message "Installing Docker Compose..."
# DOCKER_COMPOSE_VERSION="v2.20.2" # Check for the latest version
# sudo curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
# sudo chmod +x /usr/local/bin/docker-compose
# # Verify installation (optional):
# # docker-compose --version

print_message "Prerequisites: Ensure Git, Docker, and Docker Compose are installed."
echo "If not, uncomment and run the installation sections in this script, or install them manually."
echo "You might need to log out and log back in for Docker group changes to take effect."
echo ""


# --- 2. Clone Repository ---
if [ -d "$PROJECT_DIR" ]; then
  print_message "Project directory '$PROJECT_DIR' already exists. Pulling latest changes..."
  cd "$PROJECT_DIR"
  git pull
else
  print_message "Cloning repository from $GITHUB_REPO_URL..."
  git clone "$GITHUB_REPO_URL" "$PROJECT_DIR"
  cd "$PROJECT_DIR"
fi
echo ""

# --- 3. Configure .env File for Docker Compose ---
print_message "Configuring .env file for Docker Compose..."
if [ -f ".env" ]; then
  echo ".env file already exists. Please ensure it contains your OPENAI_API_KEY."
  echo "Current content (first line): $(head -n 1 .env)"
else
  echo "Creating .env file with a placeholder API key."
  echo "IMPORTANT: You MUST replace '$OPENAI_API_KEY_PLACEHOLDER' in the .env file with your actual OpenAI API key."
  echo "OPENAI_API_KEY=\"$OPENAI_API_KEY_PLACEHOLDER\"" > .env # Ensure quotes for placeholder
  echo "File '.env' created. PLEASE EDIT IT NOW with your actual API key if you haven't."
fi
echo "Press Enter to continue after you've ensured the .env file is correctly set up, or Ctrl+C to abort."
read -r
echo ""

# --- 4. Build and Run Docker Compose ---
print_message "Building and running Docker services in detached mode..."
echo "This may take a few minutes, especially on the first run..."
sudo docker-compose up --build -d

echo ""
print_message "Deployment initiated!"
echo "Services are starting in the background."
echo "To check the status and logs, navigate to the project directory ('$PWD') and run:"
echo "  sudo docker-compose ps"
echo "  sudo docker-compose logs -f"
echo ""
echo "If everything started correctly, the Spring AI application should be accessible at:"
echo "  http://<YOUR_EC2_PUBLIC_IP>:8080/api/v1/process-nl"
echo "(Replace <YOUR_EC2_PUBLIC_IP> with your EC2 instance's public IP address)."
echo "Ensure your EC2 Security Group allows inbound traffic on port 8080."
