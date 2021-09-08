<template>
	<div>
		<el-container>
			<el-main>
				<el-card>
					<div style="text-align: left;" slot="header">
						<el-page-header @back="$router.push({path: '/'})" content="我的订单">
						</el-page-header>
					</div>
					<div>
						<el-table v-loading="loading" :data="indents" stripe :header-cell-style="()=>{return 'text-align: center; font-size: 18px;';}" :cell-style="()=>{return 'text-align: center; font-size: 18px;';}">
							<el-table-column type="expand" width="30">
								<template slot-scope="scope">
									<el-descriptions border>
										<el-descriptions-item>
											<template slot="label">
												<i class="iconfont icon-credentials_icon"></i>
												乘客身份证号码
											</template>
											{{scope.row.id}}
										</el-descriptions-item>
										<el-descriptions-item>
											<template slot="label">
												<i class="el-icon-user"></i>
												乘客姓名
											</template>
											{{scope.row.name}}
										</el-descriptions-item>
										<el-descriptions-item>
											<template slot="label">
												<i class="el-icon-s-management"></i>
												订单状态
											</template>
											<el-tag effect="dark" :type="scope.row.purchased?(checkStatus(scope.row.startTime)?'success':''):((now - new Date(scope.row.purchaseTime)) / 6E4) >= 30?'danger':'warning'">
												<i :class="scope.row.purchased?'el-icon-s-claim':(((now - new Date(scope.row.purchaseTime)) / 6E4) >= 30?'el-icon-s-release':'el-icon-s-order')"></i>
												{{scope.row.purchased?(checkStatus(scope.row.startTime)?'未出行':'已出行'):leftTime(scope.row.purchaseTime)}}
											</el-tag>
										</el-descriptions-item>
										<el-descriptions-item v-if="scope.row.purchased && checkStatus(scope.row.startTime)">
											<template slot="label">
												操作
											</template>
											<el-button type="warning" @click="ticketId=scope.row.ticket;oldDate=scope.row.startTime.split(' ')[0];chooseDate=true;"><i class="el-icon-s-promotion"></i>改签</el-button>
											<el-popconfirm style="margin-left: 10px" title="您确定要退票吗？" icon-color="#F56C6C" confirm-button-type="danger" @confirm="beforeRefund(scope.row.ticket)">
												<el-button type="danger" slot="reference"><i class="el-icon-delete-solid"></i>退票</el-button>
											</el-popconfirm>
										</el-descriptions-item>
										<el-descriptions-item v-if="!scope.row.purchased && ((now - new Date(scope.row.purchaseTime)) / 6E4) < 30">
											<template slot="label">
												操作
											</template>
											<el-button type="primary" @click="pay(scope.row.ticket)"><i class="el-icon-s-finance"></i>付款</el-button>
											<el-button type="danger" style="margin-left: 10px" @click="dontWant(scope.row.ticket)"><i class="el-icon-delete-solid"></i>取消订单</el-button>
										</el-descriptions-item>
										<el-descriptions-item v-if="!checkStatus(scope.row.startTime)">
											<template slot="label">
												操作
											</template>
											<el-popconfirm style="margin-left: 10px" title="删除后您将无法再看见该订单" icon-color="#F56C6C" confirm-button-type="danger" @confirm="del(scope.row.ticket)">
												<el-button type="danger" slot="reference"><i class="el-icon-delete-solid"></i>删除订单</el-button>
											</el-popconfirm>
										</el-descriptions-item>
									</el-descriptions>
								</template>
							</el-table-column>
							<el-table-column prop="ticket" label="订单序号" width="200"></el-table-column>
							<el-table-column prop="name" label="乘客姓名" width="150" show-overflow-tooltip></el-table-column>
							<el-table-column prop="train" label="列车车次" width="100"></el-table-column>
							<el-table-column prop="startTime" sortable label="起始车站">
								<template slot-scope="scope">
									<div>
										<span style="font-size: 20px">{{scope.row.from}}站</span>
									</div>
									<div>
										<span style="font-weight: bold">{{scope.row.startTime.split(' ')[0]}}</span>
									</div>
									<div>
										<span style="font-weight: bold"><i class="el-icon-time"></i>{{scope.row.startTime.split(' ')[1]}}</span>
									</div>
								</template>
							</el-table-column>
							<el-table-column label="到达车站">
								<template slot-scope="scope">
									<div>
										<span style="font-size: 20px">{{scope.row.to}}站</span>
									</div>
									<div>
										<span style="font-weight: bold">{{scope.row.endTime.split(' ')[0]}}</span>
									</div>
									<div>
										<span style="font-weight: bold"><i class="el-icon-time"></i>{{scope.row.endTime.split(' ')[1]}}</span>
									</div>
								</template>
							</el-table-column>
							<el-table-column prop="purchaseTime" sortable label="订单时间">
								<template slot-scope="scope">
									<div>
										<span style="font-weight: bold">{{scope.row.purchaseTime.split(' ')[0]}}</span>
									</div>
									<div>
										<span style="font-weight: bold"><i class="el-icon-time"></i></span>
									</div>
									<div>
										<span style="font-weight: bold">{{scope.row.purchaseTime.split(' ')[1]}}</span>
									</div>
								</template>
							</el-table-column>
							<el-table-column label="旅途时长" width="100">
								<template slot-scope="scope">
									<span>{{getTotalTime(scope.row.startTime, scope.row.endTime)}}</span>
								</template>
							</el-table-column>
							<el-table-column prop="carriage" label="车厢座位" width="150">
								<template slot-scope="scope">
									<span>{{scope.row.carriage}}号车厢{{scope.row.seat}}</span>
								</template>
							</el-table-column>
							<el-table-column prop="type" label="车票类型" width="100"></el-table-column>
							<el-table-column label="车票价格" width="100">
								<template slot-scope="scope">
									<span>￥{{scope.row.price}}</span>
								</template>
							</el-table-column>
						</el-table>
					</div>
				</el-card>
			</el-main>
		</el-container>

		<div>
			<el-dialog title="选择出行日期" :visible.sync="chooseDate" top="30vh" @open="date=undefined">
				<div>
					<el-form>
						<el-form-item>
							<el-date-picker v-model="date" type="date" style="width: 50%" :picker-options="accessDate" value-format="yyyy-MM-dd" placeholder="请选择出发日期(仅限未来15天内)" @change="checkDate">
							</el-date-picker>
						</el-form-item>
					</el-form>
					<el-button type="primary" @click="reboot" style="width: 30%" :disabled="date===undefined"><i class="el-icon-s-ticket"></i>确认改签</el-button>
				</div>
			</el-dialog>

			<el-dialog title="支付" :visible.sync="paying">
				<template>
					<span v-html="payHtml"></span>
				</template>
			</el-dialog>

			<el-dialog title="退款验证码" :visible.sync="refundCaptchaing" width="30%" top="30vh">
				<template>
					<el-form>
						<el-form-item>
							<el-input placeholder="请输入6位验证码" v-model="captcha"></el-input>
						</el-form-item>
					</el-form>
					<el-button type="primary" @click="refund" :loading="refunding"><i class="el-icon-s-check"></i>提交验证码</el-button>
				</template>
			</el-dialog>
		</div>
	</div>
</template>

<script>
import $ajax from "../ajax";
export default {
	data() {
		return {
			indents: [],
			endorsing: false,
			chooseDate: false,
			date: "",
			oldDate: "",
			ticketId: "",
			accessDate: {
				disabledDate(time) {
					return (
						time.getTime() < Date.now() ||
						time.getTime() > Date.now() + 1000 * 3600 * 24 * 15
					);
				},
			},
			loading: true,
			now: undefined,
			interval: undefined,
			payHtml: "",
			paying: false,
			refundCaptchaing: false,
			refunding: false,
			captcha: "",
		};
	},
	methods: {
		async getMyIndents() {
			this.loading = true;
			let res = await $ajax.post(
				"/user/myIndents",
				{},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.indents = res.data.data;
			}
			this.loading = false;
		},
		async reboot() {
			let res = await $ajax.post(
				"/service/reboot",
				{
					ticket: this.ticketId,
					days:
						(new Date(this.date) - new Date(this.oldDate)) /
						1000 /
						3600 /
						24,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.getMyIndents();
				this.$notify({
					title: "改签成功",
					message: "您的改签请求已处理完毕",
					type: "success",
					duration: 1500,
				});
				this.chooseDate = false;
			} else {
				this.$notify({
					title: "改签失败",
					message: res.data.data,
					type: "error",
					duration: 1500,
				});
			}
		},
		async dontWant(ticketId) {
			this.loading = true;
			let res = await $ajax.post(
				"/service/dontWant",
				{
					ticket: ticketId,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.getMyIndents();
				this.$notify({
					title: "订单取消成功",
					message: "该订单已取消，请不要恶意订票",
					type: "success",
					duration: 0,
				});
			} else {
				this.$notify({
					title: "订单取消失败",
					message: res.data.data,
					type: "error",
					duration: 1500,
				});
			}
			this.loading = false;
		},
		async del(ticketId) {
			this.loading = true;
			let res = await $ajax.post(
				"/service/deleteTicket",
				{
					ticket: ticketId,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.getMyIndents();
				this.$notify({
					title: "订单移除成功",
					message:
						"该订单已成功移除，接下来它将不会再出现在您的记录中",
					type: "success",
					duration: 1500,
				});
			} else {
				this.$notify({
					title: "订单移除失败",
					message: res.data.data,
					type: "error",
					duration: 1500,
				});
			}
			this.loading = false;
		},
		checkDate() {
			if (this.date == this.oldDate) {
				this.date = undefined;
				this.$message({
					message: "改签日期没有改变！",
					type: "error",
					duration: 1500,
				});
			}
		},
		async beforeRefund(ticketId) {
			this.loading = true;
			let res = await $ajax.post(
				"/service/beforeRefund",
				{
					ticket: ticketId,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.$notify({
					title: "系统检测到风险操作",
					message:
						"已将验证码发送至您注册时使用的邮箱" +
						res.data.data +
						"中，请输入验证码后继续",
					type: "success",
					duration: 3000,
				});
				this.refundCaptchaing = true;
			} else {
				this.$notify({
					title: "退票失败",
					message: res.data.data,
					type: "error",
					duration: 3000,
				});
			}
			this.loading = false;
		},
		async refund() {
			this.refunding = true;
			this.loading = true;
			let res = await $ajax.post(
				"/service/refund",
				{
					captcha: this.captcha,
				},
				{
					headers: {
						Authorization:
							"Bearer " + localStorage.getItem("token"),
					},
				}
			);
			if (res.data.code == 0) {
				this.getMyIndents();
				this.$notify({
					title: "退票成功",
					message: "您的退票请求已处理完毕",
					type: "success",
					duration: 1500,
				});
				this.refundCaptchaing = false;
			} else {
				this.$notify({
					title: "退票失败",
					message: res.data.data,
					type: "error",
					duration: 1500,
				});
			}
			this.loading = false;
			this.refunding = false;
		},
		checkStatus(time) {
			let offTime = new Date(time);
			let now = new Date();
			return offTime > now;
		},
		getTotalTime(startTime, endTime) {
			let start = new Date(startTime);
			let end = new Date(endTime);
			return (
				Math.floor((end - start) / 1000 / 3600) +
				"时" +
				(((end - start) / 1000) % 3600) / 60 +
				"分"
			);
		},
		leftTime(purchaseTime) {
			let dist =
				1800 - Math.floor((this.now - new Date(purchaseTime)) / 1000);
			if (dist < 0) {
				return `已超时`;
			}
			let minute = Math.floor(dist / 60);
			let second = dist % 60;
			return `待支付 ${minute.toString().padStart(2, "0")}:${second
				.toString()
				.padStart(2, "0")}`;
		},
		async pay(ticketId) {
			window.location.href = `http://121.4.250.38:8080/ctrain/alipay/pay?ticket=${ticketId}`;
		},
	},
	created() {
		if (localStorage.getItem("token") == null) {
			this.$router.replace("/");
		}
		this.getMyIndents();
		this.interval = setInterval(() => {
			this.now = new Date();
		}, 1000);
	},
	beforeDestroy() {
		clearInterval(this.interval);
	},
};
</script>