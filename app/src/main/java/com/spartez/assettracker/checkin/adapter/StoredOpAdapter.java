package com.spartez.assettracker.checkin.adapter;

import android.content.Context;

import com.spartez.assettracker.checkin.R;
import com.spartez.assettracker.checkin.domain.Profile;

import java.util.List;

public class StoredOpAdapter extends AbstractNameAndIdViewAdapter {
    public StoredOpAdapter(Context context, Profile profile, int resource, List<NameAndIdWrapper> storedOps) {
        super(context, profile, resource, storedOps);
    }

    @Override
    protected int getSpinnerId() {
        return R.layout.storedop_selector_spinner;
    }

    @Override
    protected int getTextId() {
        return R.id.storedop_selector_spinner_entry_text;
    }

    @Override
    protected boolean isSelected(NameAndIdWrapper storedOp) {
        return storedOp != null && storedOp.getId() == profile.getOperationId();
    }
}
