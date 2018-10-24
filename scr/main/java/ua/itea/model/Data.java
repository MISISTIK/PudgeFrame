package ua.itea.model;

import java.util.Objects;

public class Data {
    private Object[] DataArray;

    public Data(int size) {
        this.DataArray = new Object[size];
        for (int i = 0 ; i < size;i++) {
            this.DataArray[i] = null;
        }
    }

    public Data(Object ... DataArray) {
        this.DataArray = new Object[DataArray.length];
        System.arraycopy(DataArray, 0, this.DataArray, 0, DataArray.length);
    }

    public Object get(int index) {
        return DataArray[index];
    }

    public void set(Object obj, int index) {
        this.DataArray[index] = obj;
    }

    public int size() {
        return DataArray.length;
    }

    public String toString() {
        StringBuffer temp = new StringBuffer();
        for (Object aDataArray : DataArray) {
            if (aDataArray == null) {
                temp.append("null").append(" | ");
            }
            temp.append(Objects.requireNonNull(aDataArray).toString()).append(" | ");
        }
        return temp.substring(0,temp.length()-3);
    }
}

