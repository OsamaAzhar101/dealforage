# Docker Image Build and Push Setup

## ✅ What I've Done

1. **Created GitHub Actions Workflow**: `.github/workflows/docker-build-push.yml`
   - Automatically builds your Maven project with Java 17
   - Creates a Docker image using your Dockerfile
   - Pushes the image to Docker Hub
   - Triggers on every push to `main` branch

2. **Pushed Changes**: The workflow is now live in your repository

## 🔧 Setup Required (One-Time)

To make this workflow functional, you need to add Docker Hub credentials to your GitHub repository:

### Step 1: Get Docker Hub Credentials
- If you don't have a Docker Hub account, create one at https://hub.docker.com
- Generate an access token: Docker Hub → Account Settings → Security → New Access Token

### Step 2: Add Secrets to GitHub Repository
1. Go to: https://github.com/OsamaAzhar101/dealforage/settings/secrets/actions
2. Click "New repository secret"
3. Add two secrets:
   - **Name**: `DOCKER_USERNAME`
     **Value**: Your Docker Hub username
   
   - **Name**: `DOCKER_PASSWORD`
     **Value**: Your Docker Hub access token (NOT your password)

## 🚀 How It Works

Once secrets are configured:

1. **Automatic**: Every time you push to `main`, GitHub Actions will:
   - Build your Java application
   - Create a Docker image
   - Push it to Docker Hub as `<your-username>/dealforage:latest`

2. **Manual**: You can also trigger it manually:
   - Go to: https://github.com/OsamaAzhar101/dealforage/actions
   - Select "Build and Push Docker Image"
   - Click "Run workflow"

## 📦 Accessing Your Docker Image

After the workflow runs successfully:

```bash
# Pull your image
docker pull <your-docker-username>/dealforage:latest

# Run locally
docker run -p 8080:8080 <your-docker-username>/dealforage:latest
```

## 🔄 For Render Deployment

Since Render auto-deploys from your GitHub repository:
- Your MainView.java changes are already pushed
- Render should automatically detect the push and redeploy
- Check your Render dashboard: https://dashboard.render.com

The Docker image on Docker Hub is an additional option for deployment flexibility.

## ⚡ Quick Status Check

- ✅ Code changes pushed to GitHub
- ✅ Docker workflow created and pushed
- ⏳ Waiting for Docker Hub secrets to be configured
- ⏳ Render auto-deployment should be in progress

## 📍 Next Steps

1. Add Docker Hub secrets to GitHub (see "Setup Required" above)
2. Check Render dashboard for deployment status
3. Once deployed, visit: https://dealforage-application.onrender.com/
4. You should see "Extracted data from : https://dealforager.com/api/products" at the top

