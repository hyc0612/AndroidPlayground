package com.example.myapplication;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Bundle;
import android.util.SizeF;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.myapplication.databinding.FragmentFirstBinding;

import java.util.ArrayList;

public class FirstFragment extends Fragment {

    private FragmentFirstBinding binding;

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.buttonFirst.setOnClickListener(v ->
                NavHostFragment.findNavController(FirstFragment.this)
                        .navigate(R.id.action_FirstFragment_to_SecondFragment)
        );

        CameraManager manager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        ArrayList<FOV> list = calculateFOV(manager);
        StringBuilder sb = new StringBuilder();
        for (FOV fov : list) {
            String facing;
            switch (fov.id) {
                case CameraCharacteristics.LENS_FACING_BACK: facing = "BACK"; break;
                case CameraCharacteristics.LENS_FACING_FRONT: facing = "FRONT"; break;
                case CameraCharacteristics.LENS_FACING_EXTERNAL: facing = "EXTERNAL"; break;
                default: facing = "?"; break;
            }
            sb.append("camera: ").append(fov.id).append(" ").append(facing)
                    .append("    w: ").append(fov.w)
                    .append("    h: ").append(fov.h)
                    .append("\n");
        }
        binding.textviewFirst.setText(sb.toString());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private static final String TAG = "XMAAAA";

    private class FOV {
        int id;
        double h;
        double w;
    }

    private ArrayList<FOV> calculateFOV(CameraManager cManager) {
        ArrayList<FOV> list = new ArrayList<>();
        try {
            for (final String cameraId : cManager.getCameraIdList()) {
                CameraCharacteristics characteristics = cManager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (true/*cOrientation == CameraCharacteristics.LENS_FACING_BACK ||
                        cOrientation == CameraCharacteristics.LENS_FACING_FRONT ||
                        cOrientation == CameraCharacteristics.LENS_FACING_EXTERNAL*/) {
                    float[] maxFocus = characteristics.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                    SizeF size = characteristics.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                    double w = size.getWidth();
                    double h = size.getHeight();
                    double horizonalAngle = 2*Math.atan(w/(maxFocus[0]*2.0));
                    double verticalAngle = 2*Math.atan(h/(maxFocus[0]*2.0));
                    FOV fov = new FOV();
                    fov.id = cOrientation;
                    fov.h = horizonalAngle * 180.0 / Math.PI;
                    fov.w = verticalAngle * 180.0 / Math.PI;
                    list.add(fov);
                    //Log.d(TAG, "camera " + cOrientation + "      h : " + horizonalAngle + "      w : " + verticalAngle);
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return list;
    }

}