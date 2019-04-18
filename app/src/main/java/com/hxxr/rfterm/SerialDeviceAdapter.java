package com.hxxr.rfterm;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class SerialDeviceAdapter extends RecyclerView.Adapter<SerialDeviceAdapter.MyViewHolder> {
    private List<SerialDevice> deviceList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView name, address, type, classId;
        public ImageView icon;

        public MyViewHolder(View view) {
            super(view);
            name = view.findViewById(R.id.name);
            address = view.findViewById(R.id.address);
            type = view.findViewById(R.id.type);
            classId = view.findViewById(R.id.classid);
            icon = view.findViewById(R.id.icon);
        }
    }


    public SerialDeviceAdapter(List<SerialDevice> deviceList) {
        this.deviceList = deviceList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.serial_device, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        SerialDevice device = deviceList.get(position);
        holder.name.setText(device.getName());
        holder.address.setText(device.getAddress());
        holder.type.setText(device.getType());
        holder.classId.setText("(0x"+Integer.toString(device.getClassId(), 16)+")");
        holder.icon.setImageResource(device.getIcon());
    }

    @Override
    public int getItemCount() {
        return deviceList.size();
    }
}
