FROM python:3.9-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    ffmpeg \
    libsndfile1 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY streamlitapp.py .
COPY app.py .
COPY __init__.py .

EXPOSE 8501

CMD ["streamlit", "run", "streamlitapp.py", "--server.address=0.0.0.0"]


