#!/bin/bash

TAG="$1"  # Tag provided as the first argument
CUR_DIR=$(pwd)  # Current directory
cd "${CUR_DIR}"/utilities  # Change directory to utilities

# Define directories and URLs for downloads
PROMETHEUS_DIR="prometheus"
GRAFANA_DIR="grafana"
JMX_DIR="jmx_prometheus"
PROMETHEUS_URL="https://github.com/prometheus/prometheus/releases/download/v2.50.1/prometheus-2.50.1.darwin-amd64.tar.gz"
PROMETHEUS_TAR="prometheus-2.50.1.darwin-amd64"
GRAFANA_URL="https://dl.grafana.com/oss/release/grafana-10.4.0.linux-amd64.tar.gz"
GRAFANA_TAR="grafana-v10.4.0"
JMX_URL="https://repo1.maven.org/maven2/io/prometheus/jmx/jmx_prometheus_javaagent/0.20.0/jmx_prometheus_javaagent-0.20.0.jar"
JMX_TAR="jmx_prometheus_javaagent-0.20.0.jar"

# Function to download and unpack files
download_and_unpack() {
  local url="$1"
  local dir="$2"
  local tar="$3"

  # Check if directory exists, if not create it
  if [ ! -d "$dir" ]; then
    mkdir -p "$dir"
  fi

  # Check if download and unpack are required based on tar name
  case "$tar" in
    "$JMX_TAR")
      if [ ! -f "$dir/$tar" ]; then
        wget "$url" -P "$dir"
        echo "Downloaded $url"
      else
        echo "$dir/$tar already exists. Skipping download."
      fi
      ;;
    "$GRAFANA_TAR" | "$PROMETHEUS_TAR")
      if [ ! -d "$dir/$tar" ]; then
        wget -qO- "$url" | tar -xzf - -C "$dir"
        echo "Downloaded and extracted $url to $dir"
      else
        echo "$dir/$tar already exists. Skipping download."
      fi
      ;;
  esac
}

# Download and unpack Prometheus
#download_and_unpack "$PROMETHEUS_URL" "$PROMETHEUS_DIR" "$PROMETHEUS_TAR"

# Download and unpack Grafana
#download_and_unpack "$GRAFANA_URL" "$GRAFANA_DIR" "$GRAFANA_TAR"

# Download JMX jars
download_and_unpack "$JMX_URL" "$JMX_DIR" "$JMX_TAR"

echo "------Downloaded and unpacked Prometheus, Grafana, and JMX.----"

cd ..

mvn clean package  # Build the Maven project

HOST_IP=$(ipconfig getifaddr en0 | awk '{print $NF}')  # Get host IP address

# Update the Prometheus YAML file using sed
sed -i '' "s/YourHostIP/$HOST_IP/g" ./utilities/prometheus.yml

# Build the vertx app image
docker image build -t java-vertx-sample:"$TAG" .

# Run the vertx app container
docker run -d -p 8888:8888 -p 12345:12345 java-vertx-sample:"$TAG"

# Run Prometheus container with mounted YAML file
docker run -d -p 9090:9090 -v ./utilities/prometheus.yml:/etc/prometheus/prometheus.yml prom/prometheus

# Run Grafana container
docker run -d -p 3000:3000 --name=grafana grafana/grafana-oss