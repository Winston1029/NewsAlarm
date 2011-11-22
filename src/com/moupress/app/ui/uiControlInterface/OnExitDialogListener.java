package com.moupress.app.ui.uiControlInterface;

import com.moupress.app.Const.SHARED_METHODS;

public interface OnExitDialogListener {
	
	public void onExitDialogFinish(boolean exit);
	public void onTwitterSelected();
	public void onFacebookSelected();
	public void onSharedMsgSend(SHARED_METHODS method,String msg);
}
