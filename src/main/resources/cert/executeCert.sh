#!/bin/bash
pwd

cd cert/certificates

#Clear all certs from before
rm -rf root* server*
# Generate root key
openssl genrsa -out root.key 2048

# Generate root certificate
openssl req -x509 -new -nodes -key root.key -sha256 -days 365 -out root.crt -subj "/CN=localhost"

# Generate server key
openssl genrsa -out server.key 2048

# Generate server certificate signing request (CSR)
openssl req -new -key server.key -out server.csr -subj "/CN=localhost"

# Create a temporary config file for certificate extensions
cat << EOF > extensions.cnf
[ server_cert ]
basicConstraints = CA:FALSE
nsCertType = server
keyUsage = digitalSignature, keyEncipherment
extendedKeyUsage = serverAuth
subjectAltName = IP:YourHostIP
EOF


# Sign server certificate with root CA
openssl x509 -req -in server.csr -CA root.crt -CAkey root.key -CAcreateserial -out server.crt -days 365 -sha256 -extfile extensions.cnf -extensions server_cert

# Remove temporary config file
rm extensions.cnf

cd ../..

#cd certificates
cp -a /cert/certificates/. .
cd ..