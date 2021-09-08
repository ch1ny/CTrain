<template>
	<div class="content">
		<el-card class="card-box">
			<el-result icon="warning" style="padding: 0%">
				<template v-if="tricked" slot="icon">
					<el-image :src="require('@/assets/icon/agms.jpg')" style="width: 70vw; height: 70vw;"></el-image>
				</template>
				<template slot="title">
					<span class="title">
						{{tricked?'あげませんーっ！！！':'你在用手机浏览对吧'}}
					</span>
				</template>
				<template slot="extra" v-if="!tricked">
					<div class="extra">
						<el-divider></el-divider>
						<div>
							可是我没做移动端的适配
						</div>
						<el-divider>☢</el-divider>
						<div>
							移动端的体验堪称噩梦
						</div>
						<el-divider></el-divider>
						<div>
							您确定要用移动端访问吗
						</div>
						<el-divider></el-divider>
						<el-button type="danger" @click="dialogVisible=true;">⚠让我康康 (震声)</el-button>
					</div>
				</template>
				<template slot="extra" v-else>
					<div class="extra">
						<div>
							👴必不可能给你看移动端
						</div>
						<el-divider></el-divider>
						<div>
							听话，去电脑上浏览
						</div>
						<el-divider></el-divider>
						<div>
							🚫也别用IE浏览器
						</div>
					</div>
				</template>
			</el-result>
		</el-card>

		<el-dialog title="这么说 🕶 你很勇咯？" :visible.sync="dialogVisible" width="80%" top="30vh">
			<div style="font-size: 20px;">喂🔞前面可是地狱啊！</div>
			<div style="font-size: 20px;">真的不考虑用电脑吗？</div>
			<span slot="footer" class="dialog-footer">
				<el-button type="primary" @click="trick">👴:“坚持访问”</el-button>
			</span>
		</el-dialog>

		<el-dialog :visible.sync="isTricking" :close-on-click-modal="false" :close-on-press-escape="false" :show-close="false" width="80%" top="30vh">
			<el-card>
				<div style="font-size: 20px;">
					正在为您跳转...<br />
					{{countdown}} 秒后回到首页...
				</div>
			</el-card>
		</el-dialog>
	</div>
</template>

<script>
export default {
	data() {
		return {
			dialogVisible: false,
			isTricking: false,
			tricked: false,
			countdown: 3,
		};
	},
	methods: {
		trick() {
			this.isTricking = true;
			let counter = setInterval(() => {
				if (this.countdown == 0) {
					clearInterval(counter);
					this.dialogVisible = false;
					this.isTricking = false;
					this.tricked = true;
					return;
				}
				this.countdown--;
			}, 1000);
		},
	},
};
</script>

<style scoped>
@import "../../assets/fonts/fonts.css";
.card-box {
	width: 80vw;
	position: relative;
	display: inline-block;
	margin-inline: auto;
	margin-bottom: 5vh;
	margin-top: 5vh;
}
.title {
	font-family: "华文仿宋";
	font-size: 20px;
}
.extra {
	font-family: "JZJDKT";
	font-size: 20px;
}
.el-button {
	font-family: "JZJDKT";
	font-size: 20px;
}
</style>