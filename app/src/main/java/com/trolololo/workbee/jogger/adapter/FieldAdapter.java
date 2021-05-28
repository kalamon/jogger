package com.trolololo.workbee.jogger.adapter;

import android.content.Context;

import com.google.common.base.Objects;
import com.trolololo.workbee.jogger.R;
import com.trolololo.workbee.jogger.domain.Profile;

import java.util.List;

public class FieldAdapter  extends AbstractNameAndIdViewAdapter {
    public FieldAdapter(Context context, Profile profile, int resource, List<NameAndIdWrapper> fields) {
        super(context, profile, resource, fields);
    }

    @Override
    protected int getSpinnerId() {
        return R.layout.field_selector_spinner;
    }

    @Override
    protected int getTextId() {
        return R.id.field_selector_spinner_entry_text;
    }

    @Override
    protected boolean isSelected(NameAndIdWrapper field) {
        return field != null && Objects.equal(field.getName(), profile.getField());
    }
}
