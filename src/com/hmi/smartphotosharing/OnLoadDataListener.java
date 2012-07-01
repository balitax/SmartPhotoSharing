package com.hmi.smartphotosharing;

// Container Activity must implement this interface
public interface OnLoadDataListener {
	
	public DrawableManager getDrawableManager();
	
	/**
	 * Should be called when a fragment needs to download data.
	 * This tells the implementing class (the activity) that the
	 * connection flags should be updated.
	 */
    public void onLoadData();	
    
    /**
     * Queries the implementing class whether downloading data
     * should be allowed, meaning that the preferences flags
     * are checked.
     * @return true if downloading is currently allowed
     */
    public boolean canLoad();
    
}
