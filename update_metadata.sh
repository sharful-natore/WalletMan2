#!/bin/bash
jq '.name = "Finance Note"' metadata.json > temp.json && mv temp.json metadata.json
