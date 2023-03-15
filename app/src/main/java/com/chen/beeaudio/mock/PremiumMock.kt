package com.chen.beeaudio.mock

import com.chen.beeaudio.model.audio.Premium

val PremiumMock = Premium(
    uid = 9900619251,
    card_id = "4M68BE544Z1Z84HO3I19BNWG0",
    card_type = 0,
    server_in = "2023-01-06T11:02:00+08:00",
    server_expired = "2024-01-06T11:02:00+08:00"
)

val EmptyPremiumMock = Premium(
    uid = 0,
    card_id = "",
    card_type = 0,
    server_in = "0001-01-01T00:00:00Z",
    server_expired = "0001-01-01T00:00:00Z"
)

val PremiumGroupNumberString : String = """
    {
		"apply_numbers": [
            {
				"uid": 9900792030,
				"screen_name": "用户9900792030",
				"description": "",
				"profile_image_url": "/user/avatar/9900792030.jpg",
				"create_at": "2023-01-03T12:03:42+08:00"
			}
        ],
		"formal_numbers": [
            {
				"uid": 9900619251,
				"screen_name": "陈大明1998",
				"description": "没有什么好说的",
				"profile_image_url": "/user/avatar/9900619251.jpg",
				"create_at": "2023-01-03T15:17:07+08:00"
			},
			{
				"uid": 9900935303,
				"screen_name": "陈大明有话要说",
				"description": "niko~niko~ni~",
				"profile_image_url": "/user/avatar/9900935303.jpg",
				"create_at": "2023-01-03T11:48:10+08:00"
			},
			{
				"uid": 9900427578,
				"screen_name": "嘉嘉子0099",
				"description": "饲服务器君是个变态～",
				"profile_image_url": "/user/avatar/9900427578.jpg",
				"create_at": "2023-01-03T10:52:00+08:00"
			}
		]
	}
""".trimIndent()

val PremiumGroupString : String = """
    {
		"numbers": [
			{
				"uid": 9900935303,
				"screen_name": "陈大明有话要说",
				"description": "niko~niko~ni~",
				"profile_image_url": "/user/avatar/9900935303.jpg",
				"create_at": "2023-01-03T11:48:10+08:00"
			},
			{
				"uid": 9900427578,
				"screen_name": "嘉嘉子0099",
				"description": "饲服务器君是个变态～",
				"profile_image_url": "/user/avatar/9900427578.jpg",
				"create_at": "2023-01-03T10:52:00+08:00"
			}
		],
		"summarize": {
			"uid": 9900427578,
			"card_id": "NVUT9MOY4TG4VWBXNER1YKA6V",
			"card_type": 1,
			"server_in": "2023-02-13T18:07:47+08:00",
			"server_expired": "2024-02-13T17:27:39+08:00"
		}
	}
""".trimIndent()