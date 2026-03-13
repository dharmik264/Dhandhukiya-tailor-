#!/usr/bin/env bash
cd tailorhub_backend_1
pip install -r requirements.txt
python manage.py collectstatic --noinput
python manage.py migrate
