package com.tehike.client.dtc.multiple.app.project.ui.display;

import java.io.Serializable;
import java.util.List;

/**
 * Created by ZKTH
 * Time ： 2018/12/7.9:11
 * data ： 所有报警点的实体类
 */

public class AllPointAddress  implements Serializable{

    private List<CamerasBean> cameras;

    private List<TerminalsBean> terminals;

    public List<CamerasBean> getCameras() {
        return cameras;
    }

    public void setCameras(List<CamerasBean> cameras) {
        this.cameras = cameras;
    }

    public List<TerminalsBean> getTerminals() {
        return terminals;
    }

    public void setTerminals(List<TerminalsBean> terminals) {
        this.terminals = terminals;
    }

    @Override
    public String toString() {
        return "AllPointAddress{" +
                "cameras=" + cameras +
                ", terminals=" + terminals +
                '}';
    }

    public static class CamerasBean {
        /**
         * guid : {501a5999-4096-465f-b70d-866b80efc66f}
         * location : {"x":444,"y":259}
         * mapUrl : http://19.0.0.229:80/Beijing256.jpg
         * name : 走廊外
         */

        private String guid;
        private LocationBean location;
        private String mapUrl;
        private String name;

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public LocationBean getLocation() {
            return location;
        }

        public void setLocation(LocationBean location) {
            this.location = location;
        }

        public String getMapUrl() {
            return mapUrl;
        }

        public void setMapUrl(String mapUrl) {
            this.mapUrl = mapUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public static class LocationBean {
            /**
             * x : 444
             * y : 259
             */

            private int x;
            private int y;

            public int getX() {
                return x;
            }

            public void setX(int x) {
                this.x = x;
            }

            public int getY() {
                return y;
            }

            public void setY(int y) {
                this.y = y;
            }
        }
    }

    public static class TerminalsBean {
        /**
         * guid : {088b2338-8b07-49ee-b9f7-7911c1b38782}
         * location : {"x":722,"y":517}
         * mapUrl : http://19.0.0.229:80/Beijing256.jpg
         * name : 哨位台（单屏）
         */

        private String guid;
        private LocationBeanX location;
        private String mapUrl;
        private String name;

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public LocationBeanX getLocation() {
            return location;
        }

        public void setLocation(LocationBeanX location) {
            this.location = location;
        }

        public String getMapUrl() {
            return mapUrl;
        }

        public void setMapUrl(String mapUrl) {
            this.mapUrl = mapUrl;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public static class LocationBeanX {
            /**
             * x : 722
             * y : 517
             */

            private int x;
            private int y;

            public int getX() {
                return x;
            }

            public void setX(int x) {
                this.x = x;
            }

            public int getY() {
                return y;
            }

            public void setY(int y) {
                this.y = y;
            }
        }
    }
}
