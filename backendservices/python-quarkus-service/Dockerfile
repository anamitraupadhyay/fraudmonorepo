FROM python:3.9-slim

WORKDIR /app

COPY requirements.txt .
# Fix numpy/pandas incompatibility
RUN pip uninstall -y numpy pandas
RUN pip install numpy==1.20.3
RUN pip install --no-cache-dir -r requirements.txt

COPY model.pkl .
COPY app.py .
COPY balanced_dataset.csv .
COPY model_train.py .

EXPOSE 5000

CMD ["python", "app.py"]
