/*
 * Copyright (c) 2016 咖枯 <kaku201313@163.com | 3772304@qq.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package cn.leeffee.feige.ui.cloud.api;

public class HostType {

    /**
     * 多少种Host类型
     */
    public static final int TYPE_COUNT = 3;

    /**
     * 时光网host  http://api.m.mtime.cn/PageSubArea/TrailerList.api
     */
    public static final int HOST_MTIME = 1;

    /**
     * 干货host   http://gank.io/api/data/Android/40/1
     */
    public static final int HOST_GANK = 2;
    /**
     * 干货host   http://172.16.60.202/
     */
    public static final int HOST_USPACE = 3;
}
