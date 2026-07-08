#!/bin/bash
jq '.name = "Sanchay"' metadata.json > temp.json && mv temp.json metadata.json
