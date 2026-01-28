#!/bin/bash

# 🧪 EJEMPLO DE PRUEBA PARA EL ENDPOINT DE CONSUMO DE PRODUCTOS

echo "🚀 Probando el endpoint de registro de consumo de productos..."

# Configuración
API_BASE_URL="http://localhost:8088"  # Ajusta el puerto según tu configuración
ORGANIZATION_ID="ORG001"
PRODUCT_ID="PROD123"
USER_ID="USER001"

echo "📍 Endpoint: $API_BASE_URL/api/v1/inventory-movements/consumption"

# Test 1: Registrar consumo de producto
echo "📦 Test 1: Registrando consumo de 5 unidades..."

curl -X POST \
  "$API_BASE_URL/api/v1/inventory-movements/consumption" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "organizationId": "'$ORGANIZATION_ID'",
    "productId": "'$PRODUCT_ID'",
    "quantity": 5,
    "unitCost": 45.50,
    "movementReason": "USO_INTERNO",
    "userId": "'$USER_ID'",
    "referenceDocument": "REQ-001",
    "referenceId": "purchase-order-123",
    "observations": "Consumo para proyecto ABC - Prueba desde script",
    "previousStock": 100,
    "newStock": 95
  }' | jq '.'

echo ""
echo "⏱️  Esperando 2 segundos..."
sleep 2

# Test 2: Obtener último movimiento
echo "🔍 Test 2: Obteniendo último movimiento del producto..."

curl -X GET \
  "$API_BASE_URL/api/v1/inventory-movements/last-movement/$ORGANIZATION_ID/$PRODUCT_ID" \
  -H "Accept: application/json" | jq '.'

echo ""
echo "⏱️  Esperando 2 segundos..."
sleep 2

# Test 3: Obtener todos los movimientos de la organización
echo "📋 Test 3: Obteniendo todos los movimientos de la organización..."

curl -X GET \
  "$API_BASE_URL/api/v1/inventory-movements/organization/$ORGANIZATION_ID" \
  -H "Accept: application/json" | jq '.'

echo ""
echo "✅ Pruebas completadas!"

# Test 4: Prueba de error - stock inconsistente
echo ""
echo "❌ Test 4: Probando validación de stock inconsistente..."

curl -X POST \
  "$API_BASE_URL/api/v1/inventory-movements/consumption" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "organizationId": "'$ORGANIZATION_ID'",
    "productId": "'$PRODUCT_ID'",
    "quantity": 5,
    "unitCost": 45.50,
    "movementReason": "USO_INTERNO",
    "userId": "'$USER_ID'",
    "previousStock": 100,
    "newStock": 98
  }' | jq '.'

echo ""
echo "🏁 Script de pruebas finalizado!"
